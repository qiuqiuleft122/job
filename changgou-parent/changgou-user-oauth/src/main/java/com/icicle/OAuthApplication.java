package com.icicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.spring.annotation.MapperScan;

/*****
 * @Author: Max
 * @Date: 2019/7/6 8:01
 * @Description: 认证授权启动类
 ****/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.icicle.user.feign"})  //开启feign客户端支持
@MapperScan(basePackages = "com.icicle.auth.dao")
public class OAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OAuthApplication.class,args);
    }


    @Bean(name = "restTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}