package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 基于死信实现延迟消息-消息配置类
 */
@Configuration
public class DeadLetterMqConfig {


    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";


    /**
     * 声明交换机
     */
    @Bean
    public DirectExchange exchange() {
        //参数1:交换机名称  参数二2:持久化 参数 3:自动删除-当交换机没有绑定队列
        return new DirectExchange(exchange_dead, true, false);
    }


    /**
     * 声明队列一:为该队列设置过期时间,该队列不会有消费者
     * 设置如果队列一 出现问题，则通过参数转到exchange_dead，routing_dead_2 上！
     */
    @Bean
    public Queue queue1() {
        HashMap<String, Object> map = new HashMap<>();
        // 参数绑定 此处的key 固定值，不能随意写，共设置三个
        // 当前队列绑定的死信交换机
        map.put("x-dead-letter-exchange", exchange_dead);
        // 死信交换机到普通队列的路由key是什么
        map.put("x-dead-letter-routing-key", routing_dead_2);
        // 设置延迟时间
        map.put("x-message-ttl", 10 * 1000);
        //参数1:队列名称 参数2:持久化 参数3:排他队列 第一次初始化该队列连接有效,该连接关闭,队列删除 参数4:自动删除 当队列没有绑定交换机 参数5:队列设置信息
        return new Queue(queue_dead_1, true, true, false, map);
    }


    /**
     * 将交换机跟队列1进行绑定
     * 通过routing_dead_1 key 绑定到exchange_dead 交换机上
     */
    @Bean
    public Binding binding1() {
        // 将队列一 通过routing_dead_1 key 绑定到exchange_dead 交换机上
        return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
    }


    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue queue2() {
        return new Queue(queue_dead_2, true, true, false);
    }


    /**
     * 设置队列二的绑定规则，将交换机跟队列1进行绑定
     */
    @Bean
    public Binding binding2() {
        // 将队列二通过routing_dead_2 key 绑定到exchange_dead交换机上！
        return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
    }
}
