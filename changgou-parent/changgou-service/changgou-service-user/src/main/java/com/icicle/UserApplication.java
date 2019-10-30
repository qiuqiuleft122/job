package com.icicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author Max
 * @Date 14:11 2019/9/5
 * @Description：用户服务启动类
 **/
@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.icicle.user.dao"})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }
}
