package com.atguigu.gmall.common.cache;


import java.lang.annotation.*;

/**
 * 自定义注解
 * 作用：优先从缓存中获取数据+避免缓存击穿采用分布式锁
 * 用来修饰注解的注解称为：元注解
 * 1.Target 注解可以被修饰位置  TYPE：类 METHOD:方法  FIELD：属性
 * 2.Retention 注解生命周期
 * 3.Inherited 注解是否可以被继承
 * 4.Documented 产生文档
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {


    /**
     * 设置存入redis缓存业务数据Key前缀
     * @return
     */
    String prefix() default "cache:";


    /**
     * 设置存入redis缓存业务数据Key后缀
     * @return
     */
    String suffix() default ":info";


}
