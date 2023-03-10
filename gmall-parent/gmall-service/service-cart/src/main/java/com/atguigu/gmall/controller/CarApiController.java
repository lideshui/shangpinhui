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
     * 用户将商品加入到购物车执行到方法
     *
     * @param skuId   商品SKUID
     * @param skuNum  商品加购的数量
     * @param request 请求对象
     */
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum, HttpServletRequest request) {
        //声明用户ID：可能是登录用户ID也可能是临时用户ID，优先使用已登陆的
        String userId = "";

        //使用公共类直接获从请求头中获取，因为网关已经将其放入到请求头中了，先尝试获取已登陆的
        userId = AuthContextHolder.getUserId(request);

        //如果登陆用户ID是空的，说明未登陆，再尝试获取临时的
        if(StringUtils.isBlank(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }

        //调用业务层添加购物车方法
        cartService.addToCart(userId, skuId, skuNum);
        return Result.ok();
    }



    /**
     * 查询用户购物车列表
     * 将两个购物车中商品合并
     * @param request
     * @return
     */
    @GetMapping("/cartList")
    public Result<List<CartInfo>> cartList(HttpServletRequest request){
        //获取登陆用户ID
        String userId = AuthContextHolder.getUserId(request);
        //获取临时用户ID
        String userTempId = AuthContextHolder.getUserTempId(request);

        //调用Service层方法进行查询当前用户的购物车列表。若两个ID都存在则进行合并
        List<CartInfo> cartInfoList = cartService.cartList(userId, userTempId);
        return Result.ok(cartInfoList);
    }


    //  选中状态
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request){

        String userId = AuthContextHolder.getUserId(request);
        //  判断
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法
        cartService.checkCart(userId,isChecked,skuId);
        return Result.ok();
    }

    /**
     * 删除
     *
     * @param skuId
     * @param request
     * @return
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        // 如何获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            // 获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId, userId);
        return Result.ok();
    }

}
