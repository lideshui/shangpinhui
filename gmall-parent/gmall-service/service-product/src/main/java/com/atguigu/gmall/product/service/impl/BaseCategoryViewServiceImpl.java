package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.model.BaseCategoryView;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 根据三级分类创建的视图的数据表对应的Service实现类
 */
@Service
public class BaseCategoryViewServiceImpl extends ServiceImpl<BaseCategoryViewMapper,BaseCategoryView> implements BaseCategoryViewService {

}
