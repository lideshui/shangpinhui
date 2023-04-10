package com.atguigu.gmall.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall.cart.model.CartInfo;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.SkuInfo;
import com.atguigu.gmall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;


    @Override
    public void addToCart(String userId, Long skuId, Integer skuNum) {
        //1.构建购物车结构 redisKey 包含登录用户ID或者临时用户ID
        //1.1 调用方法，转换后的形式：user:1:cart
        String redisKey = getCartKey(userId);

        //1.2 创建hash操作对象 - 由传入的key的决定操作数据
        //BoundHashOperations<redisKey类型，hashKey类型，hashValue类型>
        BoundHashOperations<String, String, CartInfo> hashOps = redisTemplate.boundHashOps(redisKey);

        //2.参数中skuID作为hashKey 标识商品  注意：hashKey类型必须是字符串
        //最终存入的格式为：redisKey(user:1:cart) : hash{skuId1:skuInfo, skuId2:skuInfo...}
        String hashKey = skuId.toString();

        //3.构建hashVal 将商品信息+数量
        //3.1 远程调用商品微服务，获取当前加购物车的商品信息
        SkuInfo skuInfo = productFeignClient.getSkuInfoAndImages(skuId);
        //如果获取到了添加购物车的商品信息，说明该商品存在，才有必要继续
        if (skuInfo != null) {
            CartInfo cartInfo = null;
            //判断在Redis中该商品的Id是否存在于该用户的购物车中，存在的话只用修改Redis中的加购数量
            if (hashOps.hasKey(skuId.toString())) {
                //取出购物车中的商品数据，累加数量
                cartInfo = hashOps.get(skuId.toString());
                cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            } else {
                //3.2如果不存在，则封装商品购物车对象准备创建购物车数据
                cartInfo = new CartInfo();
                cartInfo.setSkuId(skuId);
                cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
                cartInfo.setSkuName(skuInfo.getSkuName());
                cartInfo.setSkuNum(skuNum);
                cartInfo.setIsChecked(1);
                cartInfo.setCreateTime(new Date());
                cartInfo.setUpdateTime(new Date());
                cartInfo.setUserId(userId);
                cartInfo.setCartPrice(productFeignClient.getSkuPrice(skuId));
                cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            }

            //3.3 将数据存入Redis，无论修改还是新增都要写回Redis
            //redisTemplate.opsForHash().put(redisKey, hashKey, cartInfo);
            hashOps.put(hashKey, cartInfo);
        }

    }


    /**
     * 查询用户购物车列表
     * 版本1：分别查询未登录购物车列表，以及登录的购物车列表
     * 版本2：将两个购物车中商品合并
     *
     * @param userId
     * @param userTempId
     */
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {
        //1. 用户未登录(临时用户)，返回临时用户加购商品列表
        //存储临时用户的购物车列表
        List<CartInfo> noLoginCartList = null;
        //若传入的临时用户Id不为空
        if (StringUtils.isNotBlank(userTempId)){
            //1.1构建未登陆用户的redisKey
            String noLoginCartKey = getCartKey(userTempId);

            //1.2通过临时用户的ID，从Redis中获取未登的购物车列表
            BoundHashOperations<String,String,CartInfo> noLogHashOps = redisTemplate.boundHashOps(noLoginCartKey);

            //得到未登陆的购物车集合
            noLoginCartList = noLogHashOps.values();
        }

        //对临时用户加购商品集合的数据，根据更新日期来排序，只有登陆用户id为空时，需要返回他，才有必要排⚠️
        if(StringUtils.isBlank(userId)){
            //根据返回值来进行排序，通过返回负数、0、正数来控制o1和o2两个对象的大小关系⚠️
            noLoginCartList.sort((o1,o2)->{
                //参数1和参数2 分别是要比较的两个日历对象，参数3指定了比较的精度范围。
                //返回整数类型，参数1大返回1，参数1小返回-1，参数1和参数2相等返回0
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
            });
            //若未登陆直接返回即可，因为不存在登陆用户购物车列别，则不需要合并，临时用户购物车列表就是全部购物车数据⚠️
            return noLoginCartList;
        }

        //2.如果用户已经登陆并且加购了商品，并且临时用户的购物车列表中也有数据，则需要跟已登录购物车列表进行合并⚠️
        //2.1构建已登陆用户的redisKey
        String loginCartKey = getCartKey(userId);
        //2.2构建已登陆购物车hash操作对象
        BoundHashOperations<String,String,CartInfo> loginHashOps = redisTemplate.boundHashOps(loginCartKey);

        //执行合并操作⚠️⚠️⚠️
        if(!CollectionUtils.isEmpty(noLoginCartList)){
            for (CartInfo cartInfo : noLoginCartList) {
                //2.3判断如果登陆的购物车商品中包含SkuId则数量累加，不然就向Redis该用户对应的key新增该商品⚠️
                if(loginHashOps.hasKey(cartInfo.getSkuId().toString())){
                    CartInfo loginCartInfo = loginHashOps.get(cartInfo.getSkuId().toString());
                    //将临时用户购物车商品数量和登陆用户购物车数量进行累加
                    loginCartInfo.setSkuNum(cartInfo.getSkuNum() + loginCartInfo.getSkuNum());
                    //将临时购物车数据，再写到登陆的Redis哈希对象中
                    loginHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
                }else {
                    //2.4如果未登陆购物车中商品在已登陆购物车中不存在时则新增
                    //修改下UserId，将临时用户id修改为登陆用户id，再进行put即可
                    cartInfo.setUserId(userId);
                    cartInfo.setUpdateTime(new Date());
                    loginHashOps.put(cartInfo.getSkuId().toString(),cartInfo);
                }
            }

            //3. 删除未登录购物车数据，直接从Redis中删除即可
            String noLoginCartKey = getCartKey(userTempId);
            redisTemplate.delete(noLoginCartKey);
        }

        //4. 再次查询登陆后用户购物车列表
        List<CartInfo> allCartInfoList = loginHashOps.values();
        //4.对购物车商品数据进行排序
        allCartInfoList.sort((o1, o2) -> {
            return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
        });
        return allCartInfoList;

    }


    /**
     * 修改购物车选中状态
     * 用户每次勾选购物车的多选框，都要把当前状态保存起来。
     * 由于可能会涉及更频繁的操作，所以这个勾选状态不必存储到数据库中。保留在缓存状态即可。
     *
     * @param userId
     * @param isChecked
     * @param skuId
     */
    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        //获取用户购物车Key
        String cartKey = getCartKey(userId);
        //获取hash操作对象
        BoundHashOperations<String, String, CartInfo> hashOps = redisTemplate.boundHashOps(cartKey);
        if (hashOps.hasKey(skuId.toString())) {
            //获取商品信息
            CartInfo cartInfo = hashOps.get(skuId.toString());
            //修改状态
            cartInfo.setIsChecked(isChecked);
            //更新购物车商品
            hashOps.put(skuId.toString(), cartInfo);
        }
    }


    /**
     * 删除购物车中的商品-从Redis中删除🍀🍀🍀
     *
     * @param skuId
     * @param userId
     */
    @Override
    public void deleteCart(Long skuId, String userId) {
        //获取用户购物车Key
        String cartKey = getCartKey(userId);
        //获取hash操作对象
        BoundHashOperations<String, String, CartInfo> hashOps = redisTemplate.boundHashOps(cartKey);
        //删除购物车商品
        hashOps.delete(skuId.toString());
    }


    /**
     * 根据用户ID查询用户购物车中已勾选的商品列表为创建订单准备数据-从Redis中查🍀🍀🍀
     *
     * @param userId
     */
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        //1.构建查询购物车Hash结构的redisKey
        String cartKey = getCartKey(userId.toString());

        //2.查询用户所有的购物车商品
        //根据redisKey创建该key的操作对象
        BoundHashOperations<String, String, CartInfo> hashOps = redisTemplate.boundHashOps(cartKey);
        //获取所有属性值，即当前登陆用户的商品加购列表⚠️
        List<CartInfo> cartInfoList = hashOps.values();

        //3.过滤商品为选中的商品，注意掌握stream的filter用法⚠️
        if(!CollectionUtils.isEmpty(cartInfoList)){
            List<CartInfo> cartCheckedList = cartInfoList.stream().filter(cartInfo -> {
                //过滤条件 购物车对象中 isChecked 为1 则为选中的符合条件的商品
                return cartInfo.getIsChecked() == 1;
                //收集购物车中被选中的商品
            }).collect(Collectors.toList());
            //直接返回被选中的商品集合
            return cartCheckedList;
        }
        return null;
    }


    //抽取出来构建购物车redisKey的方法 user:1:cart 的方法
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

}
