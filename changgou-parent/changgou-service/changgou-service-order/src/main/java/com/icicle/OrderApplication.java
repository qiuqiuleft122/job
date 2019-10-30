package com.icicle;

import entity.FeignInterceptor;
import entity.IdWorker;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author Max
 * @Date 19:13 2019/9/8
 * @Description:订单启动类
 **/
@SpringBootApplication
@EnableEurekaClient
@EnableRabbit
@EnableFeignClients(basePackages = {"com.icicle.goods.feign","com.icicle.user.feign"})
@MapperScan(basePackages = {"com.icicle.order.dao"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }

    /**
     * 创建拦截器Bean对象
     * @return
     */
    @Bean
    public  FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }

    /**
     * 创建IDWorker对象
     */
    @Bean
    public IdWorker idWorker(){
        //机器号
        return new IdWorker(0,0);
    }
}
