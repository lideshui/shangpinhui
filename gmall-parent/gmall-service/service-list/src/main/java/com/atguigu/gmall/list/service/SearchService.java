package com.atguigu.gmall.list.service;

import com.atguigu.gmall.list.model.SearchParam;
import com.atguigu.gmall.list.model.SearchResponseVo;

public interface SearchService {
    //商品上架
    void upperGoods(Long skuId);

    //商品下架
    void lowerGoods(Long skuId);

    //更新商品的热度排名分值
    void incrHotScore(Long skuId);

    //商品检索，业务项的检索，过滤项的聚合
    SearchResponseVo search(SearchParam searchParam);
}

