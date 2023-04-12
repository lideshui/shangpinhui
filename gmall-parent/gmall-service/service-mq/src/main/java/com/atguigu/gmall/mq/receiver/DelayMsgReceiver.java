package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 基于延迟插件x-delay-message实现延迟消息-消息接收方
 */
@Slf4j
@Component
public class DelayMsgReceiver {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 监听延迟插件实现-延迟消息
     *
     * @param msg
     * @param message
     * @param channel
     */
    @RabbitListener(queues = {DelayedMqConfig.queue_delay_1})
    public void processDeadLetter(String msg, Message message, Channel channel) {
        //消费结果会发送三次，也被消费三次，所以需要保证消息幂等性，防止重复消费
        String key = "mq:" + msg;
        try {
            if (StringUtils.isNotBlank(msg)) {
                //控制同样一个消息只能被消费一次：增加幂等性处理机制-基于redis的setnx保证⚠️⚠️⚠️
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "", 10, TimeUnit.MINUTES);
                if (!flag) {
                    //说明该业务数据已经被执行
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    return;
                }
                if (StringUtils.isNotBlank(msg)) {
                    log.info("该业务首次执行，延迟插件监听到消息：{}", msg);
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("监听延迟消息异常:{}", e);
            redisTemplate.delete(key);
        }
    }
}
