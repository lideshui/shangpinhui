package com.atguigu.gmall.product.client.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 服务降级作用:保护调用方
 *   当目标服务无法访问(宕机,目标服务实例没有多余线程处理请求),进行服务降级
 *   不再发起http请求,直接走本地方法实现.快速失败机制，快速返回null⚠️
 */
//加入到IoC容器中
@Component
public class ProductDegradeFeignClient implements ProductFeignClient {
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return null;
    }

    @Override
    public List<SpuPoster> getSpuPosterBySpuId(Long spuId) {
        return null;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return null;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return null;
    }

    @Override
    public String getSkuValueIdsMap(Long spuId) {
        return null;
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {
        return null;
    }

    @Override
    public BaseTrademark getTrademarkById(Long tmId) {
        return null;
    }


}