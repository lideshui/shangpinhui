package com.atguigu.gmall.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//注意不要加 @RequestBody，我们不返回Json，直接返回模板页面
@Controller
public class IndexController {
    @Autowired
    private ProductFeignClient productFeignClient;


    //渲染首页
    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        //1.远程获取分类数据
        List<JSONObject> list = productFeignClient.getBaseCategoryList();

        //2.添加数据到模型对象Model
        model.addAttribute("list", list);

        //3.返回模板页面
        return "/index/index.html";
    }
}
