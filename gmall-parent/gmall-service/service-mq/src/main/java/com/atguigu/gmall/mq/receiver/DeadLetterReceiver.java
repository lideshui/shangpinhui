package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 基于死信实现延迟消息-消息接收方
 */
@Slf4j
@Component
public class DeadLetterReceiver {

    /**
     * 监听延迟消息
     *
     * @param msg
     * @param message
     * @param channel
     */
    @RabbitListener(queues = {DeadLetterMqConfig.queue_dead_2})
    public void processDeadLetter(String msg, Message message, Channel channel) {
        try {
            if (StringUtils.isNotBlank(msg)) {
                log.info("监听到消息:{}", msg);
            }
            //手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("监听延迟消息异常:{}", e);
        }
    }
}
