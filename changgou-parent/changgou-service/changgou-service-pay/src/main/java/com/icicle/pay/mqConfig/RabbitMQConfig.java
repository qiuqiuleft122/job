package com.icicle.pay.mqConfig;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @Author Max
 * @Date 16:20 2019/9/11
 * @Description：创建队列以及交换机并让队列和交换机绑定
 **/
@Configuration
public class RabbitMQConfig {

    @Autowired
    private Environment env;

    /**
     * 创建普通订单队列
     */
    @Bean
    public Queue orderQueue(){
        return new Queue(env.getProperty("mq.pay.queue.order"),true);
    }

    /**
     * 创建交换机  路由模式的
     */
    @Bean
    public Exchange orderExchange(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"),true,false);
    }

    /**
     * 普通订单队列绑定交换机
     * 首先会根据类型进行注入  类型注入失败就根据名字 注入
     * @Bean（name=“默认是构造方法的方法名  之前说的类名首字母小写”）
     * 所以队列绑定的时候可以不用加@Qulifier的那个注解
     */
    @Bean
    public Binding orderQueueExchange(Queue orderQueue,Exchange orderExchange){
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(env.getProperty("mq.pay.routing.key")).noargs();
    }

    //****************************************秒杀队列创建********************************************//
    /**
     * 创建秒杀订单队列
     */
    @Bean
    public Queue seckillOrderQueue(){
        return new Queue(env.getProperty("mq.pay.queue.seckillOrder"),true);
    }

    /**
     * 秒杀队列绑定交换机  【交换机是可以公用的】
     * 首先会根据类型进行注入  类型注入失败就根据名字 注入
     * @Bean（name=“默认是构造方法的方法名  之前说的类名首字母小写”）
     * 所以队列绑定的时候可以不用加@Qulifier的那个注解
     */
    @Bean
    public Binding seckillOrderQueueExchange(Queue seckillOrderQueue,Exchange orderExchange){
        return BindingBuilder.bind(seckillOrderQueue).to(orderExchange).with(env.getProperty("mq.pay.routing.seckillKey")).noargs();
    }
}
