package com.atguigu.gmall.product.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.model.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.impl.BaseAttrInfoServiceImpl
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo> implements BaseAttrInfoService {



    @Autowired
    private BaseAttrValueService baseAttrValueService;

    //根据1\2\3级分类id获取平台属性
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //调用持久层方法实现平台属性以及值获取-自定义SQL编写(一对多)
        return this.getBaseMapper().getAttrInfoList(category1Id, category2Id, category3Id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //1.业务校验-查询该分类下是否已有当前平台属性名称（无论修改还是新增都要校验）
        LambdaQueryWrapper<BaseAttrInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseAttrInfo::getAttrName, baseAttrInfo.getAttrName());
        queryWrapper.eq(BaseAttrInfo::getCategoryId, baseAttrInfo.getCategoryId());
        queryWrapper.eq(BaseAttrInfo::getCategoryLevel, baseAttrInfo.getCategoryLevel());

        //通过查询到的个数进行判断
        int count = this.count(queryWrapper);


        //如果当前分类的属性未被创建，则使用MybatisPlus的save方法进行创建
        if (baseAttrInfo.getId() == null) {
            //2.保存平台属性记录-保存后MyBatisPlus会给实体类中主键赋值⚠️

            //使用业务校验1，防止重复保存相同的平台属性
            if (count > 0) {
                throw new RuntimeException("该分类下已有该平台属性!");
            }

            this.save(baseAttrInfo);
        } else {
            //如果有则进行修改
            this.updateById(baseAttrInfo);
        }

        //3.批量保存平台属性值
        //3.1 先根据平台属性值列表 将"旧数据"删除掉
        LambdaQueryWrapper<BaseAttrValue> attrValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据属性ID查询该属性的所有属性值，不管创建还是修改，都全删除掉再保存⚠️
        attrValueLambdaQueryWrapper.eq(BaseAttrValue::getAttrId, baseAttrInfo.getId());
        List<BaseAttrValue> list = baseAttrValueService.list(attrValueLambdaQueryWrapper);

        //判断集合是否为空，不为空就删除所有值
        if (!CollectionUtils.isEmpty(list)) {
            //根据Stream流删除该属性所有的属性值
            List<Long> attrValIds = list.stream().map(BaseAttrValue::getId).collect(Collectors.toList());
            baseAttrValueService.removeByIds(attrValIds);
        }

        //3.2 按最新提交的平台属性值为准-进行批量保存，获取本次提交的全部属性值数据⚠️
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();


        //如果集合不为空，就遍历添加集合内每个属性值⚠️
        if (!CollectionUtils.isEmpty(attrValueList)) {
            //遍历平台属性值集合,为集合中每个对象关联平台属性
            attrValueList.stream().forEach(attrVal -> {
                //将平台属性值关联到平台属性
                attrVal.setAttrId(baseAttrInfo.getId());
            });
            baseAttrValueService.saveBatch(attrValueList);
        }
    }

}
