package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.model.BaseCategory1;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.service.BaseCategory1Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.impl.BaseCategory1ServiceImpl
 */

//继承MybatisPlus提供的父类ServiceImpl，传入mapper和pojo的泛型，⚠️
//继承BaseCategory1Service 接口，即继承了其父类IService接口中大量基础的数据操作方法，ServiceImpl类都实现了，大大简化工作内容⚠️
@Service
public class BaseCategory1ServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategory1Service {


    //不注入的话也可以，ServiceImpl源码中会帮助我们注入，即上面的第一个泛型类型的mapper⚠️
//    @Autowired
//    private BaseCategory1Mapper category1Mapper;


    //查询一级分类列表
    @Override
    public List<BaseCategory1> getCategory1() {


        //第一种方法，需要知道数据库字段，不推荐
//        QueryWrapper<BaseCategory1> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("is_deleted", "0");

        //推荐方法，不需要知道数据库字段，借助ORM映射，使用Pojo类属性即可，推荐⚠️
        LambdaQueryWrapper<BaseCategory1> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseCategory1::getIsDeleted, "0");


//        //假设注入了持久层,也可以这么使用
//        category1Mapper.selectList(queryWrapper);

        //调用自己的list方法，实际上是调用继承的IService的list方法


        return this.list(queryWrapper);


    }

}
