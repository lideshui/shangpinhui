package com.atguigu.gmall.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


@Configuration
public class RedissonConfig {

    //redis://%s:%d是Redis的连接格式，其中 %s表示Redis服务器的地址，%d 表示 Redis 服务器监听的端口号
    private static String ADDRESS_PREFIX = "redis://%s:%d";
    private int timeout = 3000;


    @Bean
    public RedissonClient redissonClient(RedisProperties prop) {
        if (StringUtils.isEmpty(prop.getHost())) {
            throw new RuntimeException("host is  empty");
        }
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(String.format(ADDRESS_PREFIX, prop.getHost(), prop.getPort()))
                .setTimeout(timeout);
        if (!StringUtils.isEmpty(prop.getPassword())) {
            singleServerConfig.setPassword(prop.getPassword());
        }
        return Redisson.create(config);
    }
}
