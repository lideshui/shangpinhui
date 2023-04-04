package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.model.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 平台属性值表 业务实现类
 */
@Service
public class BaseAttrValueServiceImpl extends ServiceImpl<BaseAttrValueMapper, BaseAttrValue> implements BaseAttrValueService {

    //根据平台属性ID查询属性值列表
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        //根据传入的属性ID，
        LambdaQueryWrapper<BaseAttrValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseAttrValue::getAttrId, attrId);
        return this.list(queryWrapper);
    }
}
