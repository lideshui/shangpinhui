package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.base.model.BaseEntity;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 品牌表 业务实现类
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {


    //品牌分页查询
    @Override
    public IPage<BaseTrademark> baseTrademarkByPage(IPage<BaseTrademark> iPage) {
        //1.page方法 第一个参数是分页对象， 第二个参数 分页查询条件
        LambdaQueryWrapper<BaseTrademark> queryWrapper = new LambdaQueryWrapper<>();
        //因为BaseEntity是所有pojo的父类，所以直接用他的update进行排序
        queryWrapper.orderByDesc(BaseEntity::getUpdateTime);
        iPage = this.page(iPage, queryWrapper);
        return iPage;
    }


}
