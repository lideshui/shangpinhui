package com.atguigu.gmall.list.service;

import com.atguigu.gmall.list.model.SearchParam;
import com.atguigu.gmall.list.model.SearchResponseVo;

public interface SearchService {

    //商品文档对象录入索引-操作ES索引库🔍🔍🔍
    void upperGoods(Long skuId);

    //商品文档对象从索引库中删除-操作ES索引库🔍🔍🔍
    void lowerGoods(Long skuId);

    //更新商品的热度排名分值，通过Redis的ZSet数据类型实现，提供给service-item服务调用，用户访问该sku时分值+1，满足十次同步到goods索引库🍀🍀🍀
    void incrHotScore(Long skuId);
}

