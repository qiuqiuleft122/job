package com.icicle.seckill.mqConfig;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @Author Max
 * @Date 17:48 2019/9/15
 * @Description：订单监听队列配置
 * 1.延时超时队列->负责数据暂时存储  Queue1
 * 2.真正监听的消息队列              Queue2
 * 3.创建交换机
 **/
@Configuration
public class QueueConfig {

    @Autowired
    private Environment env;

    /**
     * 真正监听改的消息队列 queue2
     * @return
     */
    @Bean
    public Queue seckillOrderTimeQueue(){
        return new Queue(env.getProperty("mq.pay.queue.seckillordertimer"),true);
    }

    /**
     * 秒杀交换机
     */
    @Bean
    public Exchange seckillExchange(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"),true,false);
    }

    /**
     * 延迟超时队列  负责数据暂时存储  Queue1
     * @return
     */
    @Bean
    public Queue delaySeckillOrderTimeQueue(){
        return QueueBuilder.durable(env.getProperty("mq.pay.queue.seckillordertimerdelay"))
                .withArgument("x-dead-letter-exchange",env.getProperty("mq.pay.exchange.order"))    //当前队列的消息一旦过期，则进入到死信队列交换机
                .withArgument("x-dead-letter-routing-key",env.getProperty("mq.pay.routing.delaySeckillKey"))
                .build();     //将死信队列的数据路由到指定队列中 【这里一定要写 路由key】
    }

    /**
     * 队列绑定到交换机
     * @param seckillOrderTimeQueue 将什么队列 绑定到交换机
     * @param seckillExchange
     * @return
     */
    @Bean
    public Binding basicBinding(Queue seckillOrderTimeQueue,Exchange seckillExchange){
        return BindingBuilder.bind(seckillOrderTimeQueue).to(seckillExchange)
                .with(env.getProperty("mq.pay.routing.delaySeckillKey")).noargs();
    }
}
