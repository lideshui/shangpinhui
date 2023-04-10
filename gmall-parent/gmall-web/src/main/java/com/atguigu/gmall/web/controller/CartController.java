package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.product.model.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 购物车页面
 *
 */
@Controller
public class CartController {

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 渲染查看购物车页面
     */
    @RequestMapping("cart.html")
    public String index(){
        return "/cart/index";
    }

    /**
     * 渲染添加购物车页面
     */
    @RequestMapping("addCart.html")
    public String addCart(@RequestParam(name = "skuId") Long skuId,
                          @RequestParam(name = "skuNum") Integer skuNum,
                          HttpServletRequest request){
        SkuInfo skuInfo = productFeignClient.getSkuInfoAndImages(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "/cart/addCart";
    }
}

