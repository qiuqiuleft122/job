package com.icicle.order.mqQueueConfig;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Max
 * @Date 20:53 2019/9/11
 * @Description：延迟队列配置  处理超时订单
 **/
@Configuration
public class QueueConfig {

    /** 短信发送队列 延迟缓冲（按消息）转发到消息队列 */
    private static final String DELAY_MESSAGE_QUEUE = "orderDelayMessageQueue";

    /** 交换机 */
    private static final String DIRECT_EXCHANGE = "orderListenerExchange";

    /** 短信发送队列 被监听的那个队列  也是路由地址*/
    public static final String LISTENER_MESSAGE_QUEUE = "orderMessageListenerQueue";

    /**
     * 创建队列Queue1：延迟队列  会过期  过期之后会将消息发送到队列Queue2
     * 两个.withArgument必不可少
     */
    @Bean
    public Queue orderDelayMessageQueue(){
        return QueueBuilder.durable(DELAY_MESSAGE_QUEUE)
                //死信交换机 这个消息一旦超时就会进入死信队列，绑定在死信队列交换机x-dead-letter-exchange
                // 此时死信对列里边的数据 死信对列里边的数据需要给其他交换机  参数一：死信交换机 参数二：被绑定的交换机
                .withArgument("x-dead-letter-exchange", DIRECT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", LISTENER_MESSAGE_QUEUE)   // 绑定指定的routing-key 参数一：死信路由key  参数二：路由到哪去
                .build();
    }


    /**
     * 创建队列Queue2 接收转发的消息
     */
    @Bean
    public Queue orderMessageListenerQueue(){
        return new Queue(LISTENER_MESSAGE_QUEUE,true);
    }

    /**
     * 创建一个交换机 路由模式
     */
    @Bean
    public Exchange orderListenerExchange(){
        return new DirectExchange(DIRECT_EXCHANGE,true,false);
    }

    /**
     * 队列queue2绑定到交换机上边   这里的with 里边是写的转向的 队列
     */
    @Bean
    public Binding orderListenerBind(Queue orderMessageListenerQueue,Exchange orderListenerExchange){
        return BindingBuilder.bind(orderMessageListenerQueue).to(orderListenerExchange).with(LISTENER_MESSAGE_QUEUE).noargs();
    }
}
