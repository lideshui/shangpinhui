package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.BaseCategory1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.service.BaseCategory1ServiceTest
 */

@SpringBootTest
class BaseCategory1ServiceTest {

    @Autowired
    private BaseCategory1Service baseCategory1Service;

    @Test
    public void Test1(){
        List<BaseCategory1> list = baseCategory1Service.list();
        System.out.println(list);
    }

}