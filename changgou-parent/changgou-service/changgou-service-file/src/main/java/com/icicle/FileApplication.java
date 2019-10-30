package com.icicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @Author Max
 * @Date 15:54 2019/8/27
 * @Description：分布式文件启动类
 **/
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) // 禁止数据源对象自动加载
@EnableEurekaClient
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class);
    }
}
