package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.list.model.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "service-list", fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    //更新商品的热度排名分值，通过Redis的ZSet数据类型实现，提供给service-item服务调用，用户访问该sku时分值+1，满足十次同步到goods索引库🍀🍀🍀
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    Result incrHotScore(@PathVariable("skuId") Long skuId);

    //首页的商品检索，通过搜索条件去ES索引库中查询对应的商品信息🔍🔍🔍
    @PostMapping("/api/list/inner")
    Result<Map> search(@RequestBody SearchParam searchParam);

    //商品文档对象录入索引-操作ES索引库，商品上架时远程调用🔍🔍🔍⚠️
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    Result upperGoods(@PathVariable("skuId") Long skuId);

    //商品文档对象从索引库中删除-操作ES索引库，商品上架时远程调用🔍🔍🔍⚠️
    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    Result lowerGoods(@PathVariable("skuId") Long skuId);

}