package com.icicle;

import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author Max
 * @Date 16:40 2019/8/25
 * @Description：品牌商品启动类
 **/
@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.icicle.goods.dao"}) //Dao接口包扫描->@MapperScan:tk下的包
public class GoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class);
    }

    /***
     * IdWorker ID生成器 保证id不重复
     * @return
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,0);
    }
}
