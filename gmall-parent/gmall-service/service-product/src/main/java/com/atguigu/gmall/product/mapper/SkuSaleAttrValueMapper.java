package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性值 Mapper 接口
 *
 * @author atguigu
 * @since 2023-02-24
 */
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    //根据获取SKU转换后的JSON，实现切换商品
    List<Map> getSkuValueIdsMap(Long spuId);
}
