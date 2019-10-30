package com.icicle;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @Author Max
 * @Date 10:51 2019/9/11
 * @Description：支付启动类
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})  //启动不自动加载数据库
@EnableEurekaClient
@EnableRabbit
public class WeiXinPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeiXinPayApplication.class,args);
    }
}
