package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//取消加载数据源类
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
//开启注册中心注解
@EnableDiscoveryClient
//开启OpenFeign注解
@EnableFeignClients
public class ListApp {
    public static void main(String[] args) {
        SpringApplication.run(ListApp.class, args);
    }
}