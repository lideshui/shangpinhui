package com.atguigu.gmall.gateway.filter;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

//配置CORS过滤器，解决跨域问题
//本质上对网关添加CORS，所有通过网关并符合拦截规则的请求都会被拦截，为其增加响应头信息
@Configuration
public class CorsConfig {

    //配置CorsWebFilter规则产生CORS过滤器⚠️
    @Bean
    public CorsWebFilter corsFilter() {
        //1.配置CORS跨域规则
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1.1设置允许哪些域名可以跨域访问  * 开发测试中方便   上线有改为真实服务器地址
        corsConfiguration.addAllowedOrigin("*");
        //1.2设置是否允许提交cookie
        corsConfiguration.setAllowCredentials(true);
        //1.3设置允许提交方法 PUT DELETE POST GET
        corsConfiguration.addAllowedMethod("*");
        //1.4设置允许提交头信息
        corsConfiguration.addAllowedHeader("*");
        //1.5设置预检请求有效期
        corsConfiguration.setMaxAge(3600L);

        //2.注册CORS配置
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        //第一个参数：设置过滤器拦截请求  第二个参数：Cors跨域规则
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(configurationSource);
    }

}