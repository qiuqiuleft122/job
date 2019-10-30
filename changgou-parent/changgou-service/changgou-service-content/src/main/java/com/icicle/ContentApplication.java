package com.icicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author Max
 * @Date 22:10 2019/8/30
 * @Description：content广告启动类
 **/
@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.icicle.content.dao"})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class);
    }
}
