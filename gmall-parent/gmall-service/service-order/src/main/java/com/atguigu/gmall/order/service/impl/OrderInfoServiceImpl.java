package com.atguigu.gmall.order.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.cart.model.CartInfo;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.enums.model.OrderStatus;
import com.atguigu.gmall.enums.model.PaymentType;
import com.atguigu.gmall.enums.model.ProcessStatus;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.model.OrderDetail;
import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.atguigu.gmall.user.model.UserAddress;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {


    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderDetailService orderDetailService;

    //仓库管理系统调用接口基础地址
    @Value("${ware.url}")
    private String wareUrl;

    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 汇总订单确认页面需要5个参数
     * 1.${userAddressList} 用户收件地址列表
     * 2.${detailArrayList} 送货清单列表
     * 3.${totalNum} 总商品数量
     * 4.${totalAmount} 订单总金额
     * 5.${tradeNo} 方式订单重复提交流水号
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> tradeDatas(String userId) {
        //创建汇总订单数据的map数组
        HashMap<String, Object> tradeMap = new HashMap<>();

        //1.远程调用用户微服务获取收货地址列表
        //将字符串userId转换为Long类型的userId
        Long userIdLong = Long.valueOf(userId);
        //通过远程Feign接口获取收货地址列表
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userIdLong);
        //如果收货地址列表不为空，则全都将数据放入Map
        if (!CollectionUtils.isEmpty(userAddressList)) {
            tradeMap.put("userAddressList", userAddressList);
        }

        //2.远程调用购物车微服务获取选中的购物车商品-将得到CarInfo封装为OrderDetail对象
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userIdLong);
        if (!CollectionUtils.isEmpty(cartCheckedList)) {
            //开始循环购物车中被选中的商品列表，转换为OrderDetail类型
            List<OrderDetail> orderDetailList = cartCheckedList.stream().map(cartInfo -> {
                OrderDetail orderDetail = new OrderDetail();
                //设置OrderDetail的名字
                orderDetail.setSkuName(cartInfo.getSkuName());
                //设置OrderDetail的SkuID
                orderDetail.setSkuId(cartInfo.getSkuId());
                //价格 远程获取最新商品价格，价格必须实时获取⚠️
                orderDetail.setOrderPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
                //设置OrderDetail的封面图片
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                //设置OrderDetail的加购数量
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                return orderDetail;
            }).collect(Collectors.toList());
            //全都将购物车中被选中的商品数据放入Map
            tradeMap.put("detailArrayList", orderDetailList);

            //3.总商品数量
            tradeMap.put("totalNum", cartCheckedList.size());

            //4.计算订单总金额
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderDetailList(orderDetailList);

            //5.调用计算总金额方法
            orderInfo.sumTotalAmount();
            tradeMap.put("totalAmount", orderInfo.getTotalAmount());
        }

        //避免订单重复提交，渲染订单确认页面中回显-业务唯一流水号 tradeNo ⚠️⚠️⚠️
        String tradeNo = this.generateTradeNo(userId);
        tradeMap.put("tradeNo", tradeNo);
        return tradeMap;
    }


    /**
     * 生成订单流水号来避免订单重复提交：
     * 1. 通过UUID生成流水号
     * 2. 生成将流水号存入Redis中，设置过期时间为1天
     *
     * @param userId
     * @return
     */
    public String generateTradeNo(String userId) {
        //通过UUID生成流水号
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //生成将流水号存入Redis中的RedisKey，格式为： user:1:tradeNo
        String redisKey = RedisConst.USER_KEY_PREFIX + userId + ":tradeNo";
        //向Redis存储订单流水号，过期时间为1天
        redisTemplate.opsForValue().set(redisKey, uuid, 1, TimeUnit.DAYS);
        return uuid;
    }


    /**
     * 在订单确认页面保存订单，响应成功保存的订单的ID
     *
     * @param orderInfo
     * @return
     */
    @Override
    public Long submitOrder(OrderInfo orderInfo, String tradeNo) {
        //1.避免用户采用浏览器回退避免订单多次提交
        String userId = orderInfo.getUserId().toString();

        //判断跟删除流水号不是原子操作
        //采用LUA脚本保证 判断跟删除原子操作
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                "then\n" +
                "    return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end");

        //使用用户ID拼接RedisKey，来存储订单流水号避免重复提交，格式为 user:1:tradeNo
        String redisKey = RedisConst.USER_KEY_PREFIX + userId + ":tradeNo";
        redisScript.setResultType(Long.class);

        //执行该脚本向Redis中查询订单流水号
        Long flag = (Long) redisTemplate.execute(redisScript, Arrays.asList(redisKey), tradeNo);
        if (flag.intValue() == 0) {
            //若提交的订单流水号没有在Redis中查询到，则抛异常
            throw new RuntimeException("请勿重复提交订单!");
        }

        //2.调用第三方库存系统(仓储服务)接口进行验证商品库存
        //2.1 获取订单中订单详情中的商品列表
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //创建错误信息存储数组
        List<String> errorMessage = new ArrayList<>();

        //声明集合存放异步任务-异步编排优化保存订单🌿🌿🌿
        List<CompletableFuture> completableFuturesList = new ArrayList<>();

        //如果当前订单内的商品数量不为空，就开始验证库存和价格
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            //遍历过程中判断每个商品库存以及价格是否合法
            orderDetailList.stream().forEach(orderDetail -> {
                //2.2调用第三方库存系统(仓储服务)接口进行验证商品库存

                //异步编排优化🌿🌿🌿
                CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                    boolean hashStock = this.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                    if (!hashStock) {
                        //如果库存数量不足，则向错误信息集合添加错误信息
                        errorMessage.add("商品:" + orderDetail.getSkuName() + "库存不足!");
                    }
                }, executor);
                //将异步任务加入到集合🌿🌿🌿
                completableFuturesList.add(stockCompletableFuture);

                //3. 调用商品微服务获取商品最新价格，验证商品价格是否发生变化
                //异步编排优化🌿🌿🌿
                CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                    //远程调用Feign接口获取商品最新价格⚠️
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                    //如果商品价格发生了变化
                    if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                        //3.1 将Redis缓存中的购物车中商品价格改为最新
                        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
                        BoundHashOperations<String, String, CartInfo> hashOps = redisTemplate.boundHashOps(cartKey);
                        //先查询，再修改
                        CartInfo cartInfo = hashOps.get(orderDetail.getSkuId().toString());
                        //设置最新价格
                        cartInfo.setSkuPrice(skuPrice);
                        //更新Redis中购物车中商品的数据
                        hashOps.put(cartInfo.getSkuId().toString(), cartInfo);
                        //向错误信息集合添加错误信息
                        errorMessage.add("商品:" + orderDetail.getSkuName() + "价格已失效!");
                    }
                }, executor);
                //将异步任务加入到集合🌿🌿🌿
                completableFuturesList.add(priceCompletableFuture);

            });
        }

        //多个异步任务执行-并行执行🌿🌿🌿
        //将异步任务列表 completableFuturesList 转化为CompletableFuture数组，并传入 CompletableFuture.allOf 方法中。
        //然后调用 join() 方法，等待所有的异步任务执行完毕。当所有异步任务都执行完毕时，join() 方法返回结果，程序继续往下执行。
        CompletableFuture.allOf(completableFuturesList.toArray(new CompletableFuture[completableFuturesList.size()])).join();

        //判断错误信息中是否有数据 有数据:业务验证失败 结束
        if (!CollectionUtils.isEmpty(errorMessage)) {
            throw new RuntimeException(errorMessage.stream().collect(Collectors.joining(",")));
        }

        //4.保存订单信息
        //4.1 封装订单表中其他信息，有些信息在浏览器传递的OrderInfo中并没有，需要自己设置
        //调用方法设置订单总金额
        orderInfo.sumTotalAmount();
        //设置订单原始金额
        orderInfo.setOriginalTotalAmount(orderInfo.getTotalAmount());
        //设置订单状态为未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //设置处理状态为未处理
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        //设置付款方式为支付宝
        orderInfo.setPaymentWay(PaymentType.ALIPAY.name());
        //生成订单编号(唯一)
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + new Random().nextInt(1000);
        //设置唯一的订单号
        orderInfo.setOutTradeNo(outTradeNo);


        //循环每一条订单商品详情
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            //使用stream流的join和收集方法，拼接所有商品的名称
            String tradeBody = orderDetailList.stream().map(OrderDetail::getSkuName).collect(Collectors.joining(","));
            //如果拼接后的所有商品名称过长，就截取前100个字符
            if (tradeBody.length() > 100) {
                orderInfo.setTradeBody(tradeBody.substring(0, 100));
            } else {
                orderInfo.setTradeBody(tradeBody);
            }
            //通过商品详情设置订单的商品图片
            orderInfo.setImgUrl(orderDetailList.get(0).getImgUrl());
        }
        //修改订单最新操作时间
        orderInfo.setOperateTime(new Date());
        //失效时间:24小时 超过该时间订单关闭
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        //正常按照前端提交地址ID查询用户地址信息
        orderInfo.setProvinceId(1L);
        //设置商品可退货时间为三十天
        calendar.add(Calendar.DATE, 30);
        orderInfo.setRefundableTime(calendar.getTime());

        //4.2 保存订单
        this.save(orderInfo);

        //5.保存订单明细信息
        for (OrderDetail orderDetail : orderDetailList) {
            //必须设置商品详情属于哪个订单
            orderDetail.setOrderId(orderInfo.getId());
            //设置商品来源
            orderDetail.setSourceId(1L);
            orderDetail.setSourceType("MALL");

            //6.订单中包含商品SkuID从购物车中删除
            String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
            BoundHashOperations<String, String, CartInfo> hashOps = redisTemplate.boundHashOps(cartKey);
            hashOps.delete(orderDetail.getSkuId().toString());
        }
        //批量保存订单明细
        orderDetailService.saveBatch(orderDetailList);

        //返回订单ID
        return orderInfo.getId();
    }


    /**
     * 调用第三方仓库存储系统进行验证商品库存是否充足
     *
     * @param skuId
     * @param skuNum
     */
    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        //1.按照仓储系统提供http接口发起http请求
        String url = wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum;
        //如果仓储系统中有足够的 skuId 商品库存，则返回文本格式的字符串“1”；如果库存不足或者发生其他错误，则返回空字符串⚠️
        String result = HttpClientUtil.doGet(url);
        //判断库存是否足够，足够返回true，不足返回false⚠️
        if (StringUtils.isNotBlank(result) && result.equals("1")) {
            return true;
        }
        return false;
    }


    /**
     * 查询当前登录用户订单列表以及订单明细内容
     *
     * @param page
     * @param limit
     */
    @Override
    public IPage<OrderInfo> getOrderList(IPage<OrderInfo> iPage, String userId, String status) {
        //1.获取操作订单持久层对象
        OrderInfoMapper orderInfoMapper = this.getBaseMapper();

        //2.调用自定义SQL查询
        if (StringUtils.isBlank(status)) {
            OrderStatus orderStatus = OrderStatus.UNPAID;
            status = orderStatus.name();
        }
        iPage = orderInfoMapper.getOrderList(iPage, status, userId);
        return iPage;
    }

}
