package com.atguigu.gmall.product.service;


import com.atguigu.gmall.product.model.BaseSaleAttr;
import com.atguigu.gmall.product.model.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface SpuManageService {
    //分页查询商品SPU列表
    IPage<SpuInfo> getSpuByPage(IPage<SpuInfo> infoPage, Long category3Id);


    //查询所有销售属性，创建SKU时候的下拉列表要用
    List<BaseSaleAttr> getBaseSaleAttrList();

    //创建商品SPU信息，创建时要对spu属性、图片、海报、销售属性、销售属性值进行赋值
    void saveSpuInfo(SpuInfo spuInfo);

}
