package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/mq")
public class MqController {


   @Autowired
   private RabbitService rabbitService;

   @Autowired
   private RabbitTemplate rabbitTemplate;


   /**
    * 发送普通消息
    *
    * @return
    */
   @GetMapping("/sendConfirm")
   public Result sendConfirm(String msg) {
      String exchange = "exchange.confirm";
      String routingKey = "routing.confirm";
      rabbitService.sendMessage(exchange, routingKey, msg);
      return Result.ok();
   }


   /**
    * 采用死信实现延迟消息
    *
    * @param msg
    * @return
    */
   @GetMapping("/sendDeadLetterMsg")
   public Result sendDeadLetterMsg(String msg) {
      rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, msg);
      log.info("基于死信发送延迟消息成功:{}", msg);
      return Result.ok();
   }


   /***
    * 采用延迟插件实现延迟消息
    * @param msg
    * @param delaydeTime 延迟时间 秒
    * @return
    */
   //@GetMapping("/sendDelayMsg")
   //public Result sendDealayMsg(String msg, int delaydeTime) {
   //
   //    rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, msg, message -> {
   //        //处理消息,为消息设置延迟时间
   //        message.getMessageProperties().setDelay(delaydeTime * 1000);
   //        return message;
   //    });
   //    log.info("基于延迟插件发送延迟消息成功:{}", msg);
   //    return Result.ok();
   //}

   /***
    * 采用延迟插件实现延迟消息
    * @param msg
    * @param delaydeTime 延迟时间 秒
    * @return
    */
   @GetMapping("/sendDelayMsg")
   public Result sendDelayMsg(String msg, int delaydeTime) {
      //调用封装好延迟消息发送方法
      rabbitService.sendDelayMessage(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, msg, delaydeTime);
      log.info("基于延迟插件发送延迟消息成功:{}", msg);
      return Result.ok();
   }

}
