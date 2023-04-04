package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.BaseCategoryTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 分类品牌中间表 Mapper 接口
 */
public interface BaseCategoryTrademarkMapper extends BaseMapper<BaseCategoryTrademark> {

    //直接使用注解在Mapper接口实现自定义SQL，就不需要再去XML文件中写了，但仅仅适合简单SQL语句。
    @Delete("delete bct from base_category_trademark bct where bct.category3_id = #{category3Id} and bct.trademark_id = #{trademarkId}")
    void removeCategoryTrademark(@Param("category3Id") Long category3Id, @PathVariable("trademarkId") Long trademarkId);
}
