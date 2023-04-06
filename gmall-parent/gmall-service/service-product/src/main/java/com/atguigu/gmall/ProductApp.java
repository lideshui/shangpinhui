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
 * 让启动类实现CommandLineRunner接口
 * CommandLineRunner 是 Spring Boot 提供的一个接口，用于在应用程序启动后执行一段代码逻辑。
 * 实现 CommandLineRunner 接口的类中的 run 方法会在应用程序启动后被自动执行，通常用于进行一些初始化操作、检查操作或测试等。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ProductApp.class, args);
    }


    /**=======以下内容是布隆过滤器的配置内容=======**/

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
        /**
         * 初始化布隆过滤器⚠️
         * getBloomFilter() 是Redisson 的一个 API 方法，用于获取一个布隆过滤器实例。
         * 参数是Redis 中保存布隆过滤器数据的键名⚠️
         * 泛型之所以是Long，是因为商品保存或上架时，存储的是商品ID⚠️
         */
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        /**
         * 参数1：布隆过滤器预期的数据规模，即100000 表示布隆过滤器的预期元素个数（capacity）
         * 参数2：布隆过滤器的误差率，误差率越低，则布隆过滤器的效果越好，但所需空间也越大。
         */
        bloomFilter.tryInit(100000, 0.01);
    }
}

