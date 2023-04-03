package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseAttrInfo;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.BaseAttrInfoService
 */
public interface BaseAttrInfoService {
    //根据1\2\3级分类id获取平台属性
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

}

