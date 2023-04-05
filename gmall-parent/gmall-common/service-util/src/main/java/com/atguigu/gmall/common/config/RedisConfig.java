package com.atguigu.gmall.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 配置自定义RedisTemplate对象设置Key和Value的序列化方式
 */

@Configuration
@EnableCaching
public class RedisConfig {

    @Primary
    @Bean
    public RedisTemplate<Object, Object> RedisTemplate(RedisConnectionFactory RedisConnectionFactory) {
        RedisTemplate<Object, Object> RedisTemplate = new RedisTemplate<>();
        RedisTemplate.setConnectionFactory(RedisConnectionFactory);

        //使用Jackson2JsonRedisSerialize 替换默认序列化(默认采用的是JDK序列化) 对存储的对象进行JSON序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 序列化key value
        RedisTemplate.setKeySerializer(new StringRedisSerializer());
        RedisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        RedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        RedisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        RedisTemplate.afterPropertiesSet();
        return RedisTemplate;
    }
}