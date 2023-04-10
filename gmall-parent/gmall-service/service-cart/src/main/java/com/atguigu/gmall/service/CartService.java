package com.atguigu.gmall.service;

import com.atguigu.gmall.cart.model.CartInfo;

import java.util.List;

public interface CartService  {

    //用户将商品加入到购物车
    void addToCart(String userId, Long skuId, Integer skuNum);


}
