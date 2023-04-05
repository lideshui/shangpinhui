package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性值 Mapper 接口
 */
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    //调用skuSaleAttrValueMapper，自定义SQL语句，获得{"3736|3738":"24","3736|3739":"25",}格式的数据实现商品切换⚠️
    List<Map> getSkuValueIdsMap(Long spuId);
}
