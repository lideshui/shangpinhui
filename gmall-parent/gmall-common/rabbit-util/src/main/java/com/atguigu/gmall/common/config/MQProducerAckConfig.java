package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;

/**
 * 确认消息不丢失:生成者确认手段
 *
 * @author: atguigu
 * @create: 2023-03-11 15:12
 */
@Slf4j
@Configuration
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @PostConstruct修饰方法,boot程序启动成功自动执行该方法
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }


    /**
     * 交换机确认(生产者确认),当前消息发送成功或失败,Broker会异步进行回调
     *
     * @param correlationData 相关的数据(除了业务数据外可以携带的数据)-封装路由键,交换机名称,消息内容
     * @param ack             确认结果 true:发送消息到交换机成功  false:发送消息到交换机失败
     * @param cause           失败原因,只有失败才有值
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("交换机确认(生产者确认)成功,相关数据:{}", correlationData);
        } else {
            log.error("交换机确认失败,发送消息到交互失败:{}", cause);
            //将CorrelationData接口转为实现类GmallCorrelationData
            GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;
            this.retryMessage(gmallCorrelationData);
        }
    }


    /**
     * 队列确认,该方法只有在交互机路由消息到队列异常情况才会回调该方法
     *
     * @param message    消息对象
     * @param replyCode  返回码
     * @param replyText  原因文本
     * @param exchange   交换机名称
     * @param routingKey 路由键名称
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        //对消息进行重发
        String redisKey = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        String str = (String) redisTemplate.opsForValue().get(redisKey);
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(str, GmallCorrelationData.class);
        //交换机暂存消息,消息无法及时路由到队列,会触发队列路由异常回调方法
        //解决方式一:如果是延迟消息,不处理
        if (gmallCorrelationData.isDelay()) {
            return;
        }

        //解决方式二:对重发方法进行修正-增加延迟消息重发逻辑-必然导致延迟消息多次发送
        log.error("消息路由queue失败，应答码={}，原因={}，交换机={}，路由键={}，消息={}",
                replyCode, replyText, exchange, routingKey, message.toString());
        this.retryMessage(gmallCorrelationData);
    }


    /**
     * 消息重发
     *
     * @param correlationData 相关数据
     */
    private void retryMessage(GmallCorrelationData correlationData) {
        //1.获取重试次数
        int retryCount = correlationData.getRetryCount();
        //2.判断重试次数大于等于3 - 入库记录发送异常记录,运维人员,人工处理
        if (retryCount >= 3) {
            log.error("重发次数已达上限,交给运维人员处理,将发送失败消息存入异常数据库,人工处理!");
            return;
        }
        //3.更新Redis中重试次数
        String correlationDataStr = (String) redisTemplate.opsForValue().get(correlationData.getId());
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(correlationDataStr, GmallCorrelationData.class);
        gmallCorrelationData.setRetryCount(gmallCorrelationData.getRetryCount() + 1);
        redisTemplate.opsForValue().set(correlationData.getId(), JSON.toJSONString(gmallCorrelationData));
        //处理延迟消息重发,保证消息依然延迟发送
        if (gmallCorrelationData.isDelay()) {
            //结果:对于延迟性消息立即执行重发方法
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(), gmallCorrelationData.getRoutingKey(), gmallCorrelationData.getMessage(), message -> {
                message.getMessageProperties().setDelay(gmallCorrelationData.getDelayTime() * 1000);
                return message;
            }, gmallCorrelationData);
        } else {
            //4.从相关数据中获取所有信息,用于重发消息(业务消息,相关消息)
            rabbitTemplate.convertAndSend(correlationData.getExchange(), correlationData.getRoutingKey(), correlationData.getMessage(), gmallCorrelationData);
        }
        System.out.println("消息进行重发");
    }
}
