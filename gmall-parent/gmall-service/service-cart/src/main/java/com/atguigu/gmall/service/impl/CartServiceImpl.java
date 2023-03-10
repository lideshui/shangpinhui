package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.cart.model.CartInfo;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.SkuInfo;
import com.atguigu.gmall.service.CartService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
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

    //合并查询购物车
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {
        //1.（临时用户）用户未登录：加入商品到购物车  用户没登录 返回临时用户加购商品列表
        List<CartInfo> noLoginCartList = null;
        //若传入的未登陆用户Id不为空
        if (StringUtils.isNotBlank(userTempId)){
            //1.1构建未登陆用户的redisKey
            String noLoginCartKey = getCartKey(userTempId);

            //1.2通过临时用户的ID，从Redis中获取未登的购物车列表
            BoundHashOperations<String,String,CartInfo> noLogHashOps = redisTemplate.boundHashOps(noLoginCartKey);
            //得到未登陆的购物车集合
            noLoginCartList = noLogHashOps.values();
        }

        //进行对未登陆集合的数据根据更新日期来排序，只有登陆用户id为空时，需要返回他，才有必要排
        if(StringUtils.isBlank(userId)){
            noLoginCartList.sort((o1,o2)->{
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
            });
            //若未登陆直接返回
            return noLoginCartList;
        }

        //2.如果未登录购物车列表中有数据，则需要跟已登录购物车列表进行合并--说明登录了
        //2.1构建登陆用户的redisKey
        String loginCartKey = getCartKey(userId);
        //2.2构建已登陆购物车hash操作对象
        BoundHashOperations<String,String,CartInfo> loginHashOps = redisTemplate.boundHashOps(loginCartKey);

        //执行合并操作
        if(!CollectionUtils.isEmpty(noLoginCartList)){
            for (CartInfo cartInfo : noLoginCartList) {
                //2.3判断如果登陆的购物车商品中包含SkuId则数量累加，不然就向Redis新增
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

    @Override
    public void deleteCart(Long skuId, String userId) {
        //获取用户购物车Key
        String cartKey = getCartKey(userId);
        //获取hash操作对象
        BoundHashOperations<String, String, CartInfo> hashOps = redisTemplate.boundHashOps(cartKey);
        //删除购物车商品
        hashOps.delete(skuId.toString());
    }


    //抽取出来构建购物车redisKey的方法 user:1:cart 的方法
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
