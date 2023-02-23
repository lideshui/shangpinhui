package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.BaseCategoryTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 分类品牌中间表 Mapper 接口
 *
 * @author atguigu
 * @since 2023-02-22
 */
public interface BaseCategoryTrademarkMapper extends BaseMapper<BaseCategoryTrademark> {

    @Delete("delete bct from base_category_trademark bct where bct.category3_id = #{category3Id} and bct.trademark_id = #{trademarkId}")
    void removeCategoryTrademark(@Param("category3Id") Long category3Id, @PathVariable("trademarkId") Long trademarkId);
}
