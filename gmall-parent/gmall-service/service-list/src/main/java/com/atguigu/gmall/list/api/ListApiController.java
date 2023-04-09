package com.atguigu.gmall.list.api;

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
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchService searchService;


    //创建商品索引库
    @GetMapping("/inner/createIndex")
    public Result createIndex() {
        //Elasticsearch的Java客户端工具包中的一个方法，用于向 Elasticsearch 集群发送创建 Goods 索引请求
        restTemplate.createIndex(Goods.class);
        //Elasticsearch的Java客户端工具包中的一个方法，用于向 Elasticsearch 集群发送设置 Goods 类型的索引映射请求
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }


    //商品文档对象录入索引-操作ES索引库🔍🔍🔍
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }


    //商品文档对象从索引库中删除-操作ES索引库🔍🔍🔍
    @GetMapping("/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId){
        searchService.lowerGoods(skuId);
        return Result.ok();
    }


    //更新商品的热度排名分值，通过Redis的ZSet数据类型实现，提供给service-item服务调用，用户访问该sku时分值+1，满足十次同步到goods索引库🍀🍀🍀
    @GetMapping("/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId){
        searchService.incrHotScore(skuId);
        return Result.ok();
    }
}
