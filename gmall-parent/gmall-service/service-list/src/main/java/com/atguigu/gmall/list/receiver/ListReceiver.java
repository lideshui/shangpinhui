package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
public class ListReceiver {


    @Autowired
    private SearchService searchService;


    /**
     * 监听商品上架消息,同步更新索引库商品
     *
     * @param skuId
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_GOODS),
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true"),
            key = MqConst.ROUTING_GOODS_UPPER
    ))
    public void onSaleGoods(Long skuId, Message message, Channel channel) {
        try {
            if (skuId != null) {
                log.info("商品上架监听消息:{}", skuId);
                searchService.upperGoods(skuId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("商品上架消息业务:{}处理异常:{}", skuId, e);
        }
    }



    /**
     * 监听商品下架消息,同步删除索引库商品
     *
     * @param skuId
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_GOODS),
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true"),
            key = MqConst.ROUTING_GOODS_LOWER
    ))
    public void lowerGoods(Long skuId, Message message, Channel channel) {
        try {
            if (skuId != null) {
                log.info("商品下架监听消息:{}", skuId);
                searchService.lowerGoods(skuId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("商品下架消息业务:{}处理异常:{}", skuId, e);
        }
    }
}
