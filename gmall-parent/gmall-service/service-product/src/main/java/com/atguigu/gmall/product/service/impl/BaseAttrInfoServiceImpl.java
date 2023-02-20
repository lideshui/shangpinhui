package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.impl.BaseAttrInfoServiceImpl
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo> implements BaseAttrInfoService {
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //方式二:先查询平台属性名称集合
        //遍历平台属性名称 分别 查询每个平台属性包含属性值 手动set设值


        //方式一:调用持久层方法实现平台属性以及值获取-自定义SQL编写(一对多)
        return this.getBaseMapper().getAttrInfoList(category1Id, category2Id, category3Id);
    }
}
