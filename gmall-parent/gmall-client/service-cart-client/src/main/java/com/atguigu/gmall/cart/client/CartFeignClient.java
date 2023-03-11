package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.cart.model.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * author:atguigu
 * 描述：
 **/
@FeignClient(value = "service-cart",fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {
    //  根据用户Id 获取到选中购物车列表。
    @GetMapping("api/cart/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId);

}

