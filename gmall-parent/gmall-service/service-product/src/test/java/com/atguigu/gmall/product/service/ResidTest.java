package com.atguigu.gmall.product.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class ResidTest {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test(){
        redisTemplate.opsForValue().set("sku:1315:info","v2");

    }
}
