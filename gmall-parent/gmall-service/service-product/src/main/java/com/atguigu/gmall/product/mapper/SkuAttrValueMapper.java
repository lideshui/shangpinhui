package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.atguigu.gmall.product.model.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * sku平台属性值关联表 Mapper 接口
 */
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {

    //根据SkuID查询当前商品包含平台属性以及属性值-product微服务远程调用接口⚠️
    List<BaseAttrInfo> getAttrList(Long skuId);
}
