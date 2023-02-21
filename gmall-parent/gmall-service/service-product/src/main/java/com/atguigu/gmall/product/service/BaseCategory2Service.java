package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseCategory2;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.BaseCategory1Service
 */
public interface BaseCategory2Service extends IService<BaseCategory2> {
    //查询二级分类列表
    List<BaseCategory2> getCategory2(Long category1Id);
}
