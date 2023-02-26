package com.atguigu.gmall.product.service;


import com.atguigu.gmall.product.model.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.List;

public interface SkuManageService {


    //根据spuId 查询销售属性集合
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    //添加sku
    void saveSkuInfo(SkuInfo skuInfo);

    //sku分页列表
    IPage<SkuInfo> getSkuInfoPage(Long page, Long limit,Long category3Id);

    //上架
    void onSale(Long skuId);

    //下架
    void cancelSale(Long skuId);

    //获取图片信息
    SkuInfo getSkuInfo(Long skuId);

    //商品详情的价格
    BigDecimal getSkuPrice(Long skuId);

    //根据skuid获取平台属性和属性值
    List<BaseAttrInfo> getAttrList(Long skuId);

    //根据获取SKU转换后的JSON，实现切换商品
    String getSkuValueIdsMap(Long spuId);
}
