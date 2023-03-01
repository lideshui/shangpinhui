package com.atguigu.gmall;

import com.atguigu.gmall.common.constant.RedisConst;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: atguigu
 * @create: 2022-11-27 23:04
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ProductApp.class, args);
    }

    @Autowired
    private RedissonClient redissonClient;


    /**
     * 当Springboot应用启动后执行
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        //1.初始化布隆过滤器 数据规模 误判率 - 操作Redis
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        bloomFilter.tryInit(100000, 0.01);
    }
}

