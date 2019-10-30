package com.icicle;

import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author Max
 * @Date 17:11 2019/9/12
 * @Description：秒杀微服务启动类
 **/
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.icicle.pay.feign"})
@MapperScan(basePackages = {"com.icicle.seckill.dao"})
@EnableScheduling  //开启定时
@EnableAsync  //开启支持异步执行
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class,args);
    }

    /**
     * ID生成器
     * @return
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }
}
