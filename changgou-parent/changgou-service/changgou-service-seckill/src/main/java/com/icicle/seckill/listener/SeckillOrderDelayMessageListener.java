package com.icicle.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.icicle.pay.feign.WeiXinPayFeign;
import com.icicle.seckill.pojo.SeckillOrder;
import com.icicle.seckill.service.SeckillOrderService;
import entity.Result;
import entity.SeckillStatus;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @Author Max
 * @Date 18:58 2019/9/16
 * @Description：秒杀超时订单消息监听
 **/
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillordertimer}")
public class SeckillOrderDelayMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private WeiXinPayFeign weiXinPayFeign;

    @Autowired
    private SeckillOrderService seckillOrderService;

    /***
     * 读取消息
     * 判断Redis中是否存在对应的订单
     * 如果存在，则关闭支付，再关闭订单
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            System.out.println("回滚时间："+simpleDateFormat.format(new Date()));
            //读取消息
            SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);

            //获取redis 中的 订单
            SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(seckillStatus.getUsername());

            //如果Redis中有订单信息，说明用户未支付
            if (seckillOrder != null){
                System.out.println("准备回滚---"+seckillStatus);
                //关闭支付 feign远程调用
                Result<Map<String, String>> colseResult = weiXinPayFeign.closePay(seckillStatus.getOrderId());

                Map<String, String> closeMap = colseResult.getData();
                //这里为什么要这样判断呢？因为 关闭支付 微信会返回一些信息 只有返回的状态码为SUCCESS 而且业务结果为SUCCESS的时候 删除订单
                //因为 关闭支付的过程中 有可能30分钟的时候用户在支付 这个时候超时消息读到了 那么还是需要鉴定下关闭支付返回的消息 才能确定是否关闭
                if(closeMap!=null && closeMap.get("return_code").equalsIgnoreCase("success") &&
                        closeMap.get("result_code").equalsIgnoreCase("success") ){
                    //删除订单  【实际中可能这个订单还是需要保存到数据库】
                    seckillOrderService.deleteOrder(seckillStatus.getUsername());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
