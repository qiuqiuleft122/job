package com.icicle.seckill.task;

import com.alibaba.fastjson.JSON;
import com.icicle.seckill.dao.SeckillGoodsMapper;
import com.icicle.seckill.pojo.SeckillGoods;
import com.icicle.seckill.pojo.SeckillOrder;
import entity.IdWorker;
import entity.SeckillStatus;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author Max
 * @Date 16:59 2019/9/14
 * @Description：多线程抢单异步实现
 **/
@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    /**
     * 异步执行
     * @Async:该方法会异步执行(底层多线程方式)
     */
    @Async
    public void createOrder(){
        try {
            System.out.println("准备睡会儿再下单！");
            Thread.sleep(10000);

            //从Redis队列中获取用户排队信息 [redis单线程操作 队列取出来一个就没有一个]
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();

            //没有排队信息 直接结束
            if(seckillStatus==null){
                return;
            }

            //定义要购买的商品的ID和时区以及用户名字
            String time=seckillStatus.getTime();
            Long id=seckillStatus.getGoodsId();
            String username=seckillStatus.getUsername();

            //先到SeckillGoodsCountList_ID队列中获取该商品的一个信息，如果能获取，则可以下单 [抢单排队之前先看看队列中是否有数据 没有的话就不用下单了]
            Object seckillGood = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
            //如果不能获取该商品的队列信息，则表示没有库存，清理排队信息
            if (seckillGood == null){
                //则表示没有库存，清理排队信息
                clearQueue(username);
                return;
            }

            //获取秒杀商品数据
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            //判断有没有库存
            if (seckillGoods == null || seckillGoods.getStockCount()<=0){
                //没有库存
                throw new RuntimeException("已售罄！");
            }
            //创建订单对象
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());  //秒杀订单id
            seckillOrder.setSeckillId(id); //商品id
            seckillOrder.setUserId(username);  //用户名
            seckillOrder.setCreateTime(new Date());  //订单创建时间
            seckillOrder.setMoney(seckillGoods.getCostPrice());  //支付秒杀金额
            seckillOrder.setStatus("0");  //支付状态 创建订单 肯定未支付

            /***
             * 将订单对象存储起来 redis  秒杀  操作频繁 并发高  不建议放在数据库中
             * 1.一个用户只允许有一个未支付秒杀订单
             * 2.订单存入到Redis
             *       Hash
             *          namespace->SeckillOrder
             *                          username:SeckillOrder
             */
            redisTemplate.boundHashOps("SeckillOrder").put(username,seckillOrder);

            /***
             * 库存递减
             *      Redis.stockCount--
             *      商品有可能是最后一个,如果是最后一个，则将Redis中商品信息删除,并且将Redis中该商品的数据同步到MySQL
             */
            //此处控制数量  用redis控制 ，不要用seckillGoods本身，因为每次操作都是基于内存操作，并发情况会导致数据不精准
            //redis的自增键是安全的
            Long seckillGoodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, -1); //商品库存减1
            seckillGoods.setStockCount(seckillGoodsCount.intValue()); //根据计数器统计

            //测试的时候：可以在这里 设置睡眠 那么超卖 会非常严重
            //Thread.sleep(10000);

            //判断商品是否还有库存
            // 使用redsi单线程操作后的这个库存数seckillGoodsCount  安全的
            //if(seckillGoods.getStockCount()<=0){ 之前这样 会造成数据不精准
            if (seckillGoodsCount<=0){
                //同步数据到MySQL数据库  [注意这里 是redis中的秒杀商品卖完之后 才同步到数据库]
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //如果没有库存,则清空Redis缓存中该商品
                redisTemplate.boundHashOps("SeckillGoods_"+time).delete(time);
            }else {
                //如果有库存，则将数据重置到Reids中 【redis中的秒杀商品列表库存也是需要同步的 计数的那个只是计数使用的】
                redisTemplate.boundHashOps("SeckillGoods_"+time).put(id,seckillGoods);  //覆盖之前的
            }

            //更新下单状态
            seckillStatus.setOrderId(seckillOrder.getId());  //订单id
            seckillStatus.setMoney(Float.valueOf(seckillGoods.getCostPrice()));  //应付金额
            seckillStatus.setStatus(2);  //2:秒杀等待支付
            //更新后的排队信息存入redis中
            redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);

            //订单完成  给mq发送生成订单消息  处理超时订单  这里也可以提取出来作为一个方法
            //参数一：消息发送到延迟队列 参数二(必须是Object类型)：消息 订单状态  参数三：设置延时
            rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"),
                    (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    //设置延迟时间
                    message.getMessageProperties().setExpiration("40000");
                    return message;
                }
            });
            System.out.println("下单完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 商品售罄后清空用户的排队状态啥的（包含用户重复排队标识 和 用户的排队状态）
     * @param username
     */
    private void clearQueue(String username){
        //清空排队标识【免得一致排不了队】
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //清空用户的排队状态
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }
}
