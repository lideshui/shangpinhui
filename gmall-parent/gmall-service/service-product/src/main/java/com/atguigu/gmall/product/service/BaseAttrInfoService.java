package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.BaseAttrInfoService
 */
public interface BaseAttrInfoService {
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);
}

