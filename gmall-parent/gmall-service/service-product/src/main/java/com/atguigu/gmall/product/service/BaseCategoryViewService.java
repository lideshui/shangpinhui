package com.atguigu.gmall.product.service;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.model.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseCategoryViewService extends IService<BaseCategoryView> {

    //查询所有分类列表 分类嵌套结果:一级分类分类对象中包含二级分类集合;在二级分类对象中包含三级分类集合
    List<JSONObject> getBaseCategoryList();
}
