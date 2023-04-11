package com.atguigu.gmall.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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


    /**
     * 只要是注解@PostConstruct修饰的方法，boot程序启动成功自动执行该方法
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
        log.error("消息路由queue失败，应答码={}，原因={}，交换机={}，路由键={}，消息={}",
                replyCode, replyText, exchange, routingKey, message.toString());
    }

}
