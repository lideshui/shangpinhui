package com.atguigu.gmall.product.service;


import com.atguigu.gmall.product.model.SkuInfo;
import com.atguigu.gmall.product.model.SpuImage;
import com.atguigu.gmall.product.model.SpuInfo;
import com.atguigu.gmall.product.model.SpuSaleAttr;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface SkuManageService {


    //根据spuId 查询销售属性集合，创建SKU要用
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    //添加sku
    void saveSkuInfo(SkuInfo skuInfo);

    //sku分页列表
    IPage<SkuInfo> getSkuInfoPage(Long page, Long limit,Long category3Id);

    //上架SKU-目前先简单写一下，后期会修改
    void onSale(Long skuId);

    //下架SKU-目前先简单写一下，后期会修改
    void cancelSale(Long skuId);
}
