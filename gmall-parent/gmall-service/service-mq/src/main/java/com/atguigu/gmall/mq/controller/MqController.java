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
    * 正常消息发送
    */
   @GetMapping("sendConfirm")
   public Result sendConfirm() {
      rabbitService.sendMessage("exchange.confirm", "routing.confirm", "来人了，开始接客吧！");
      return Result.ok();
   }


   /**
    * 基于死信实现延迟消息-发送延迟消息
    *
    * @param msg
    */
   @GetMapping("/sendDeadLetterMsg")
   public Result sendDeadLetterMsg(String msg) {
      rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, msg);
      log.info("基于死信发送延迟消息成功:{}", msg);
      return Result.ok();
   }

}

