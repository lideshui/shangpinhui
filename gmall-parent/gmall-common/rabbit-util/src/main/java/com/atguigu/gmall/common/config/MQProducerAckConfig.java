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
 * @Description 消息发送确认-确保生产者消息不丢失
 *
 * ConfirmCallback  只确认消息是否正确到达 Exchange 中
 * ReturnCallback   消息没有正确到达队列时触发回调，如果正确到达队列不执行
 *
 * 1. 如果消息没有到exchange,则confirm回调,ack=false
 * 2. 如果消息到达exchange,则confirm回调,ack=true
 * 3. exchange到queue成功,则不回调return
 * 4. exchange到queue失败,则回调return
 */
@Slf4j
@Configuration
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 只要是注解@PostConstruct修饰的方法，boot程序启动成功自动执行该方法，应用启动后只触发一次
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }


    /**
     * 只确认消息是否正确到达 Exchange 中,成功与否Broker会异步进行回调⚠️
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
            //1.将CorrelationData接口转为实现类GmallCorrelationData，通过相关数据构建GmallCorrelationData对象
            GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;

            //2.执行消息重发⚠️
            this.retryMessage(gmallCorrelationData);
        }
    }


    /**
     * 消息没有正确到达队列时触发回调，如果正确到达队列不执行⚠️
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
        //当路由队列失败 也需要对消息进行重发
        //1.通过redisTemplate从Redis缓存中获取到之前构建并存储的GMallCorrelationData对象（即消息的相关数据）
        String redisKey = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        String str = (String) redisTemplate.opsForValue().get(redisKey);
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(str, GmallCorrelationData.class);

        /**
         * 交换机暂存消息，消息无法及时路由到队列，会触发队列路由异常回调方法
         * 解决方式一：如果是延迟消息，直接不处理，return即可
         * 解决方式二：对重发方法进行修正-增加延迟消息重发逻辑-必然导致延迟消息多次发送
         */
        //========解决延迟队列和消息重发冲突问题-方案1⚠️⚠️⚠️========
        if (gmallCorrelationData.isDelay()) {
            return;
        }

        //2.执行消息重发⚠️
        this.retryMessage(gmallCorrelationData);
    }


    /**
     * 消息重发
     *
     * @param correlationData 相关数据
     */
    private void retryMessage(GmallCorrelationData correlationData) {
        //1.获取消息已经重试的次数；
        int retryCount = correlationData.getRetryCount();

        //2.判断重试次数是否已经达到上限(例如3次)-如果是发送异常记录，交给运维人员人工处理，并结束
        if (retryCount >= 3) {
            log.error("重发次数已达上限,交给运维人员处理,将发送失败消息存入异常数据库,人工处理!");
            return;
        }

        //3.若未到重试次数则获取Redis中的消息重发相关数据
        String correlationDataStr = (String) redisTemplate.opsForValue().get(correlationData.getId());
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(correlationDataStr, GmallCorrelationData.class);

        //4.重发消息
        //========解决延迟队列和消息重发冲突问题-方案2⚠️⚠️⚠️========
        //判断是否为延时消息
        if (gmallCorrelationData.isDelay()) {
            //结果：对于延迟性消息，依然需要设置消息延迟时间
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(), gmallCorrelationData.getRoutingKey(), gmallCorrelationData.getMessage(), message -> {
                message.getMessageProperties().setDelay(gmallCorrelationData.getDelayTime() * 1000);
                return message;
            }, gmallCorrelationData);
        } else {
            //如果是普通消息，立即执行重发方法
            rabbitTemplate.convertAndSend(correlationData.getExchange(), correlationData.getRoutingKey(), correlationData.getMessage(), gmallCorrelationData);
        }

        //5.更新Redis中相关的重试次数
        gmallCorrelationData.setRetryCount(gmallCorrelationData.getRetryCount() + 1);
        redisTemplate.opsForValue().set(correlationData.getId(), JSON.toJSONString(gmallCorrelationData));

        log.info("进行消息重发！");
    }

}
