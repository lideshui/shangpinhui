package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseCategory1;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.BaseCategory1Service
 */
public interface BaseCategory1Service extends IService<BaseCategory1> {
    //查询一级分类列表
    List<BaseCategory1> getCategory1();
}
