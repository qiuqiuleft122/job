package com.icicle.order.listener;

import com.alibaba.fastjson.JSON;
import com.icicle.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;

/**
 * @Author Max
 * @Date 16:58 2019/9/11
 * @Description：监听支付结果消息修改订单
 **/
@Component
@RabbitListener(queues = {"${mq.pay.queue.order}"})  //指定监听的队列
public class OrderPayMessageListener {

    @Autowired
    private OrderService orderService;

    /**
     * 支付结果监听
     * @param message  从MQ消息队列中监听到的消息接收
     */
    @RabbitHandler
    public void oderPayMessageListener(String message) throws ParseException {
        //支付结果
        Map<String,String> resultMap = JSON.parseObject(message, Map.class);
        System.out.println("监听到的支付结果：\n"+resultMap);

        //获取通信标识  返回的状态码 必是：SUCCESS
        String return_code = resultMap.get("return_code");
        //返回信息 说白了就是业务结果  包含了叫做result_code  -->结果 SUCCESS/FAIL
        String result_code = resultMap.get("result_code");

        if (return_code.equals("SUCCESS")){
            //代表有结果
            //获取订单号  不管支付成功 肯定会有订单号
            String out_trade_no = resultMap.get("out_trade_no");

            //支付成功修改订单状态
            if (result_code.equals("SUCCESS")){
                //修改订单状态 (此时需要将订单号 交易流水号 支付时间传到后台)
                orderService.updateOrderStatus(out_trade_no,resultMap.get("time_end"),resultMap.get("transaction_id"));
            }else {
                //关闭支付 去查询API  作业

                //支付失败,关闭支付，取消订单，回滚库存
                orderService.deleteOrder(out_trade_no);
            }
        }
    }
}
