package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseAttrValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 平台属性值表 业务接口类
 */
public interface BaseAttrValueService extends IService<BaseAttrValue> {

    //根据平台属性ID查询属性值列表
    List<BaseAttrValue> getAttrValueList(Long attrId);
}
