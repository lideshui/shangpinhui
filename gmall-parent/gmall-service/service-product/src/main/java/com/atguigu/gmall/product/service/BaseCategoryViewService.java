package com.atguigu.gmall.product.service;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.model.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 根据三级分类创建的视图的数据表对应的Service
 */
public interface BaseCategoryViewService extends IService<BaseCategoryView> {

    //查询所有分类列表 分类嵌套结果:一级分类分类对象中包含二级分类集合;在二级分类对象中包含三级分类集合-商城首页产品分类使用⚠️
    List<JSONObject> getBaseCategoryList();
}
