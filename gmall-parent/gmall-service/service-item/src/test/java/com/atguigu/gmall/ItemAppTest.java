package com.atguigu.gmall;

import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.SkuInfo;
import com.atguigu.gmall.product.model.SpuPoster;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemAppTest {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Test
    public void test(){
        String skuValueIdsMap = productFeignClient.getSkuValueIdsMap(8L);
        System.out.println(skuValueIdsMap);
    }

}