package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 品牌表 Mapper 接口
 */
public interface BaseTrademarkMapper extends BaseMapper<BaseTrademark> {

    //查询当前分类可选品牌列表
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);
}
