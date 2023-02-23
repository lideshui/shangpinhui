package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseCategoryTrademark;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.atguigu.gmall.product.model.CategoryTrademarkVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类品牌中间表 业务接口类
 * @author atguigu
 * @since 2023-02-22
 */
public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {
    //查询分类下关联的品牌列表
    List<BaseTrademark> findTrademarkList(Long category3Id);

    //查询当前分类可选品牌列表
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);


    void saveBasecategoryTrademark(CategoryTrademarkVo categoryTrademarkVo);

    void removeCategoryTrademark(Long category3Id, Long trademarkId);
}

