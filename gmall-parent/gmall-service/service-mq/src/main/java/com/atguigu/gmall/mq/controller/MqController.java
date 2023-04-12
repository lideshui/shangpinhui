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


   /**
    * 基于插件实现延时消息有两种方案：(我们这里使用方案1⚠️)
    * 方案1：消息在被发布时就设置了延迟时间，因此可以将消息直接发送到目标队列。
    * 方案2：利用了 RabbitMQ 的 TTL 特性，即设置了消息的 TTL 属性值，使消息在一定时间后失效。
    *
    * 两种方法的区别：
    * 1. 二者不同的是，第二种方案需要交换机暂存消息，从而导致交换机无法即时路由消息到队列。
    * 2. 如果需要对消息精确控制，尽可能避免消息的丢失和重复消费，可以使用第一个方法。
    * 3. 如果要求相对简单，并且不关心消息的丢失和重复消费，可以使用第二种方法。
    */


   /**
    * 方案1：发送延迟消息：基于延迟插件使用，可以将消息直接发送到目标队列。
    * 注意：使用时需要保证 RabbitMQ 安装了延迟插件并且配置正确。⚠️
    */
   @GetMapping("/sendDelayMsg")
   public Result sendDelayMsg1() {
      //调用工具方法发送延迟消息
      int delayTime = 10;
      rabbitService.sendDelayMessage(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "我是延迟消息", delayTime);
      log.info("基于延迟插件-发送延迟消息成功");
      return Result.ok();
   }


   /**
    * 方案2：发送延迟消息：基于延迟插件使用，使用插件后交换机会暂存消息，从而导致交换机无法即时路由消息到队列
    * 注意：使用时需要保证 RabbitMQ 安装了延迟插件并且配置正确。⚠️
    */
   //@GetMapping("/sendDelayMsg")
   public Result sendDelayMsg2() {
      rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay,
              DelayedMqConfig.routing_delay,
              "基于延迟插件-我是延迟消息",
              (message -> {
                 //设置消息ttl
                 message.getMessageProperties().setDelay(10000);
                 return message;
              })
      );
      log.info("基于延迟插件-发送延迟消息成功");
      return Result.ok();
   }


}

