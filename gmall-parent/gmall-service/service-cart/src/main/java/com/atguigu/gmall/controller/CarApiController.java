package com.atguigu.gmall.controller;


import com.atguigu.gmall.cart.model.CartInfo;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.service.CartService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CarApiController {

    @Autowired
    CartService cartService;


    /**
     * 用户将商品加入到购物车
     *
     * @param skuId   商品SKUID
     * @param skuNum  商品加购的数量
     * @param request 请求对象
     */
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum, HttpServletRequest request) {
        //声明用户ID：可能是登录用户ID也可能是临时用户ID，优先使用已登陆的⚠️
        String userId = "";

        //1. 使用公共类直接获从请求头中获取，因为网关已经将其放入到请求头中了，先尝试获取已登陆的
        userId = AuthContextHolder.getUserId(request);

        //2. 如果登陆用户ID是空的，说明未登陆，再尝试获取临时的
        if(StringUtils.isBlank(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }

        //调用业务层添加购物车方法
        cartService.addToCart(userId, skuId, skuNum);
        return Result.ok();
    }


}
