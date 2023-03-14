package com.atguigu.gmall.order.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.enums.model.OrderStatus;
import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: atguigu
 * @create: 2023-03-13 14:14
 */
@Slf4j
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;


    /**
     * 定时关闭订单监听器
     *
     * @param orderId 订单ID
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(queues = {MqConst.QUEUE_ORDER_CANCEL})
    public void closeOrder(Long orderId, Message message, Channel channel) {
        //1.判断消息是否有值
        try {
            if (orderId != null) {
                log.info("[订单微服务],监听关闭订单消息:{}", orderId);
                //2.根据订单ID查询订单对象 判断订单状态
                OrderInfo orderInfo = orderInfoService.getById(orderId);
                if (orderInfo != null && OrderStatus.UNPAID.name().equals(orderInfo.getOrderStatus()) && OrderStatus.UNPAID.name().equals(orderInfo.getProcessStatus())) {
                    //3.修改订单状态
                    orderInfoService.execExpiredOrder(orderId);
                }
                //4.手动应答
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("[订单微服务],关闭订单消息执行异常:", e);
            //希望再次broker再次投递消息
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }


}
