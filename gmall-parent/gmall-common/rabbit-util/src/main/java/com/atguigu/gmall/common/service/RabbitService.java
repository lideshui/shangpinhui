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


    /**
     * 基于延迟插件封装:发送延迟消息方法
     *
     * @param exchange
     * @param rountingKey
     * @param message
     * @param delayTime   延迟时间: 单位秒
     */
    public void sendDelayMessage(String exchange, String rountingKey, Object message, int delayTime) {
        //1.构建相关数据对象-设置是否为延迟消息,延迟消息时间  将相关数据存入Redis
        GmallCorrelationData correlationData = new GmallCorrelationData();
        String id = "mq:" + UUID.randomUUID().toString().replaceAll("-", "");
        correlationData.setId(id);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(rountingKey);
        correlationData.setMessage(message);
        correlationData.setDelay(true);
        correlationData.setDelayTime(delayTime);
        //后期可以动态计算相关数据有效时间进行设置⚠️
        redisTemplate.opsForValue().set(id, JSON.toJSONString(correlationData), 1, TimeUnit.HOURS);

        //2.调用发送消息方法-带着相关数据(交互机,路由键,业务消息,消息处理器,相关数据)
        rabbitTemplate.convertAndSend(exchange, rountingKey, message, msg -> {
            //设置消息延迟时间
            msg.getMessageProperties().setDelay(delayTime * 1000);
            return msg;
        }, correlationData);

        //3.将相关消息存入Redis  Key：UUID  相关消息对象  10 分钟
        //redisTemplate.opsForValue().set(uuid, JSON.toJSONString(correlationData), 10, TimeUnit.MINUTES);
        //return true;
    }

}