package com.atguigu.gmall.product.service;


import com.atguigu.gmall.product.model.BaseSaleAttr;
import com.atguigu.gmall.product.model.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface SpuManageService {
    //分页查询商品SPU列表
    IPage<SpuInfo> getSpuByPage(IPage<SpuInfo> infoPage, Long category3Id);


    //查询所有销售属性
    List<BaseSaleAttr> getBaseSaleAttrList();

    //保存
    void saveSpuInfo(SpuInfo spuInfo);

}
