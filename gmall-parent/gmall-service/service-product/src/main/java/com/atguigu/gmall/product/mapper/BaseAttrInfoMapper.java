package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.mapper.BaseAttrInfoMapper
 */
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    List<BaseAttrInfo> getAttrInfoList(@Param("category1Id") Long category1Id, @Param("category2Id") Long category2Id, @Param("category3Id") Long category3Id);
}
