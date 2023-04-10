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


    /**
     * 查询用户购物车列表
     * 版本1：分别查询未登录购物车列表，以及登录的购物车列表
     * 版本2：将两个购物车中商品合并
     *
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


    /**
     * 修改购物车选中状态
     * 用户每次勾选购物车的多选框，都要把当前状态保存起来。
     * 由于可能会涉及更频繁的操作，所以这个勾选状态不必存储到数据库中。保留在缓存状态即可。
     *
     * @param skuId
     * @param isChecked
     * @param request
     */
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request){
        // 使用公共类获取当前登陆用户的userId
        String userId = AuthContextHolder.getUserId(request);
        //  判断是否已登陆，若没登陆则获取临时用户ID
        if (StringUtils.isEmpty(userId)){
            // 获取临时用户ID
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法修改购物车选中状态
        cartService.checkCart(userId,isChecked,skuId);
        return Result.ok();
    }


    /**
     * 删除购物车中的商品-从Redis中删除🍀🍀🍀
     *
     * @param skuId
     * @param request
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        // 使用公共类获取当前登陆用户的userId
        String userId = AuthContextHolder.getUserId(request);
        //  判断是否已登陆，若没登陆则获取临时用户ID
        if (StringUtils.isEmpty(userId)) {
            // 获取临时用户ID
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法删除购物车数据
        cartService.deleteCart(skuId, userId);
        return Result.ok();
    }

}
