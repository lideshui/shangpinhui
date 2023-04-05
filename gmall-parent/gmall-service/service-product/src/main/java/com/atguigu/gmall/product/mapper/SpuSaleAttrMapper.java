package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * spu销售属性 Mapper 接口
 */
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    //根据spuId 查询销售属性集合
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);


    //查询当前商品所有的销售属性,判断为当前SKU拥有销售属性增加选中效果-product微服务远程调用接口⚠️
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);
}
