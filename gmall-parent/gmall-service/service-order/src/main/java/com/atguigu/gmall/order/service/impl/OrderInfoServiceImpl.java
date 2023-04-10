package com.atguigu.gmall.order.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.cart.model.CartInfo;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.model.OrderDetail;
import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.atguigu.gmall.user.model.UserAddress;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
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

        //避免订单重复提交,渲染订单确认页面中回显-业务唯一流水号 tradeNo ⚠️⚠️⚠️
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

}
