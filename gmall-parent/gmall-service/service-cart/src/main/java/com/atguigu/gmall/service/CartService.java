package com.atguigu.gmall.service;

import com.atguigu.gmall.cart.model.CartInfo;

import java.util.List;

public interface CartService  {

    //用户将商品加入到购物车执行到方法
    void addToCart(String userId, Long skuId, Integer skuNum);

    //查询用户购物车列表，需要合并
    List<CartInfo> cartList(String userId, String userTempId);

    //修改选中状态
    void checkCart(String userId, Integer isChecked, Long skuId);

    //删除购物车
    void deleteCart(Long skuId, String userId);

    /**
     * 查询用户购物车中已勾选的商品列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(Long userId);
}
