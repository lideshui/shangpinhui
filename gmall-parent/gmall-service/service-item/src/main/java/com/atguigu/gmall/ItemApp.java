package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
//因为使用的Nacos，该注解加不加都可
@EnableDiscoveryClient
//开启Feign的扫描，扫描后产生代理对象，根据代理对象发起请求，这个必须加⚠️⚠️⚠️
//默认扫描的包是当前包com.atguigu.gmall，可使用basePackages指定包⚠️⚠
@EnableFeignClients
public class ItemApp {
    public static void main(String[] args) {
        SpringApplication.run(ItemApp.class, args);
    }
}