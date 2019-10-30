package com.icicle.order.listener;

import com.icicle.order.mqQueueConfig.QueueConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Max
 * @Date 22:52 2019/9/11
 * @Description：延迟消息的监听 处理超时订单  (过期消息的监听)
 **/
@Component
@RabbitListener(queues = QueueConfig.LISTENER_MESSAGE_QUEUE)  //指定监听的队列 接收死信队列消息的那个队列
public class DelayMessageListener {

    /**
     * 延时队列监听
     * @param message MQ中的延迟消息
     */
    @RabbitHandler
    public void getDelayMessage(String message){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.print("监听消息的时间："+simpleDateFormat.format(new Date()));
        System.out.println(",监听到的消息："+message);

        //监听到了消息 就需要取消订单  回滚库存  关闭支付通道  后期完成

    }
}
