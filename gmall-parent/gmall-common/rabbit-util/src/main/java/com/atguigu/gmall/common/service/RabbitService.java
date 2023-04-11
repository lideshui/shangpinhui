package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * 封装发送消息工具方法(基本消息,延迟消息)
 */
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用于其他微服务发送消息工具方法
     *
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        //1.创建自定义相关消息对象-包含业务数据本身，交换器名称，路由键，队列类型，延迟时间,重试次数
        GmallCorrelationData correlationData = new GmallCorrelationData();
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);
        correlationData.setMessage(message);
        String redisKey = "mq:" + UUID.randomUUID().toString().replaceAll("-", "");
        correlationData.setId(redisKey);

        //2.将相关消息封装到发送消息方法中
        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(correlationData), 1, TimeUnit.DAYS);

        //3.将相关消息存入Redis  Key：UUID  相关消息对象  10 分钟
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        return true;
    }

}