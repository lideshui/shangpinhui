package com.atguigu.gmall.mq.receiver;


import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
public class ConfirmReceiver {


    /**
     * 注解@RabbitListener：
     * 1. 如果交换机跟队列创建, 并绑定 只需要指定监听队列名称即可
     * 2. 反之交换机跟队列没有创建,指定队列绑定交换机路由键,框架扫描到之后会自动创建
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = "routing.confirm"))
    public void process(Message message, Channel channel) {
        try {
            log.info("消息接收成功:{}", new String(message.getBody()));
            //手动确认-通知broker服务器消息正常消费
            //参数一:broker服务每个消息设置的标签 参数二:消息确认 true:批量确认 false:单个消息确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("消息接收异常");
        }
    }
}