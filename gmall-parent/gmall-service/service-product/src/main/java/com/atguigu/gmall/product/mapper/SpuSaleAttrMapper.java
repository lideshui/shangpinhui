package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * spu销售属性 Mapper 接口
 *
 * @author atguigu
 * @since 2023-02-23
 */
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    //根据spuId 查询销售属性集合
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    //获取商品详情的销售属性，以及选中效果
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId") Long spuId);

}
