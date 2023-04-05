package com.atguigu.gmall.web.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
//取消所有警告
@SuppressWarnings("all")
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    /**
     * 通过Feign远程调用微服务接口，渲染商品详情页面
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String getItem(@PathVariable("skuId") Long skuId, Model model) {
        //调用详情微服务获取渲染详情页所有的数据
        Result<Map> result = itemFeignClient.getItemAllData(skuId);
        System.out.println(JSON.toJSONString(result.getData()));
        model.addAllAttributes(result.getData());
        return "item/item";
    }

}

