package com.atguigu.gmall.list.service;

import com.atguigu.gmall.list.model.SearchParam;
import com.atguigu.gmall.list.model.SearchResponseVo;

public interface SearchService {

    //商品文档对象录入索引-操作ES索引库🔍🔍🔍
    void upperGoods(Long skuId);

    //商品文档对象从索引库中删除-操作ES索引库🔍🔍🔍
    void lowerGoods(Long skuId);
}

