package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseCategory3;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.BaseCategory3Service
 */
public interface BaseCategory3Service extends IService<BaseCategory3> {
    //查询二级分类列表
    List<BaseCategory3> getCategory3(Long category2Id);
}
