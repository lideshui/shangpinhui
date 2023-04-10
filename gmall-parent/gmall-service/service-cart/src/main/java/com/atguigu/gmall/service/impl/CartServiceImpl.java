package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.cart.model.CartInfo;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.SkuInfo;
import com.atguigu.gmall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;


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


    //抽取出来构建购物车redisKey的方法 user:1:cart 的方法
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

}
