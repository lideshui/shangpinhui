package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.model.Goods;
import com.atguigu.gmall.list.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/list")
public class ListController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchService searchService;

    //创建商品索引库
    @GetMapping("/inner/createIndex")
    public Result createIndex() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }


    //测试接口，商品文档对象录入索引
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }


    //测试接口，商品文档删除
    @GetMapping("/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId){
        searchService.lowerGoods(skuId);
        return Result.ok();
    }



}