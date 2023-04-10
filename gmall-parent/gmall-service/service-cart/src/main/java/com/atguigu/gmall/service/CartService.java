package com.atguigu.gmall.service;

import com.atguigu.gmall.cart.model.CartInfo;

import java.util.List;

public interface CartService  {

    //用户将商品加入到购物车
    void addToCart(String userId, Long skuId, Integer skuNum);

    //查询购物车列表，分别查询登陆用户和临时用户的，然后进行合并
    List<CartInfo> cartList(String userId, String userTempId);

    //修改购物车选中状态
    void checkCart(String userId, Integer isChecked, Long skuId);

    //删除购物车中的商品-从Redis中删除🍀🍀🍀
    void deleteCart(Long skuId, String userId);

    //根据用户ID查询用户购物车中已勾选的商品列表为创建订单准备数据-从Redis中查🍀🍀🍀
    List<CartInfo> getCartCheckedList(Long userId);
}
