package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseCategoryTrademark;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.atguigu.gmall.product.model.CategoryTrademarkVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类品牌中间表 业务接口类
 */
public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {
    //查询分类下关联的品牌列表
    List<BaseTrademark> findTrademarkList(Long category3Id);

    //查询当前分类可选品牌列表
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);

    //将品牌关联到分类，多选批量添加p品牌的接口
    void saveBasecategoryTrademark(CategoryTrademarkVo categoryTrademarkVo);

    //删除分类品牌关联
    void removeCategoryTrademark(Long category3Id, Long trademarkId);
}

