package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.atguigu.gmall.product.model.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * sku平台属性值关联表 Mapper 接口
 *
 * @author atguigu
 * @since 2023-02-24
 */
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {

    //RestFul方法商品详情获取平台属性
    List<BaseAttrInfo> getAttrList(Long skuId);
}
