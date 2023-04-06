package com.atguigu.gmall.product.service;


import com.atguigu.gmall.product.model.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
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

    //根据SkuID查询SKU商品信息包含图片列表-product微服务远程调用接口⚠️
    SkuInfo getSkuInfoAndImages(Long skuId);

    //根据商品SKU三级分类ID查询分类信息-product微服务远程调用接口⚠️
    BigDecimal getSkuPrice(Long skuId);

    //根据SkuID查询当前商品包含平台属性以及属性值-product微服务远程调用接口⚠️
    List<BaseAttrInfo> getAttrList(Long skuId);

    //获取每一组销售属性对应SkuID组合，来完成商品页切换-product微服务远程调用接口⚠️
    String getSkuValueIdsMap(Long spuId);
}
