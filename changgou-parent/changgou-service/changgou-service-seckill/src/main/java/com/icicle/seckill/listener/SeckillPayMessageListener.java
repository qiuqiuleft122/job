package com.icicle.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.icicle.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @Author Max
 * @Date 17:52 2019/9/15
 * @Description：秒杀订单支付结果消息监听
 **/
@RabbitListener(queues = "${mq.pay.queue.seckillOrder}")  //监听哪个队列
public class SeckillPayMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 秒杀支付消息监听
     * @param message
     */
    @RabbitHandler
    public void getPayMessage(String message){
        try {
            //将监听到的消息转换成map
            Map<String,String> resultMap = JSON.parseObject(message, Map.class);
            //return_code 微信返回的通信标识
            String return_code = resultMap.get("return_code");
            //业务结果 SUCCESS/FAIL
            String result_code = resultMap.get("result_code");
            //自定义的数据
            String attach = resultMap.get("attach");
            //转为map
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);

            if (return_code.equals("SUCCESS")){
                //result_code->业务结果-SUCCESS->改订单状态
                if (result_code.equals("SUCCESS")){
                    //改订单状态
                    seckillOrderService.updatePayStatus(attachMap.get("username"),
                            resultMap.get("transaction_id"),resultMap.get("time_end"));
                }else {
                    // FAIL ->删除订单[真实工作中存入到MySQL 叫做什么未支付订单 或者 已取消订单啥的 不会删除]->回滚库存
                    seckillOrderService.deleteOrder(attachMap.get("username"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
