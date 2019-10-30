package com.icicle.seckill.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.icicle.seckill.dao.SeckillGoodsMapper;
import com.icicle.seckill.dao.SeckillOrderMapper;
import com.icicle.seckill.pojo.SeckillGoods;
import com.icicle.seckill.pojo.SeckillOrder;
import com.icicle.seckill.service.SeckillOrderService;
import com.icicle.seckill.task.MultiThreadingCreateOrder;
import entity.IdWorker;
import entity.SeckillStatus;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/****
 * @Author:Max
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/1/28
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired(required = false)
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    /**
     * 关闭订单，回滚库存
     * @param username 用户名
     */
    @Override
    public void deleteOrder(String username) {
        //获取用户排队状态
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        //获取redsi中的订单信息
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //如果Redis中有订单信息，说明用户未支付
        if (seckillStatus != null && seckillOrder != null){
            //删除订单  [真实情况是存入mysql]
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            //回滚库存 Redis递增->Redis不一定有商品  因为下单的时候有可能是最后一个 商品删掉了 这个时候需要从数据库加载
            //1)从Redis中获取该商品
            SeckillGoods seckillGoods  = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
            //2)如果Redis中没有，则从数据库中加载
            if (seckillGoods == null) {
                //数据库查询
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            }
            //3)数量+1  (递增数量+1，队列数量+1)   自增计数器加1
            Long goodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), 1);
            seckillGoods.setStockCount(goodsCount.intValue());  //商品库存 自增键线程安全  不要在内存中操作
            //这里不用 同步到数据库 我们的机制是 等到redis中商品为0 就同步
            //队列加一
            redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillStatus.getGoodsId()).leftPush(seckillStatus.getGoodsId());

            //将数据同步到redis
            redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).put(seckillStatus.getGoodsId(),seckillGoods);

            //支付失败  清空排队标识
            clearQueue(username);
        }
    }

    /**
     * 更新redis中的订单状态  [支付成功]
     * @param username 登录的用户名
     * @param transactionId 交易的流水号
     * @param endTime 支付时间
     */
    @Override
    public void updatePayStatus(String username, String transactionId, String endTime) {
        //查询订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        try {
            if (seckillOrder != null) {
                //修改订单状态
                seckillOrder.setStatus("1");
                seckillOrder.setTransactionId(transactionId);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                seckillOrder.setPayTime(sdf.parse(endTime));
                //执行修改 同步到MySQL中  这是生成的订单 放在M有SQL中
                seckillOrderMapper.insertSelective(seckillOrder);

                //同时删除redis中的订单
                redisTemplate.boundHashOps("SeckillOrder").delete(username);
                //清空排队信息
                clearQueue(username);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            //转换异常
        }
    }

    /**
     * 支付成功之后清空用户的排队状态啥的（包含用户重复排队标识 和 用户的排队状态）
     * @param username
     */
    private void clearQueue(String username){
        //清空排队标识【免得一直排不了队】
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //清空用户的排队状态
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }

    /**
     *  查询某个用户的排队状态
     * @param username 登录的用户
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }

    /**
     * 添加秒杀订单
     * @param id 商品id
     * @param time 商品秒杀开始时间
     * @param username 用户登录名
     * @return
     */
    @Override
    public Boolean add(Long id, String time, String username) {
        //记录用户排队的次数  防止秒杀重复排队  每次进入抢单的时候，对它进行递增
        //1:"key",
        //2:"自增的值"
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if (userQueueCount>1){
            //100表示 重复排队
            throw new RuntimeException("100");
        }
        //排队信息的封装
        SeckillStatus seckillStatus = new SeckillStatus(username,new Date(),1,id,time);

        //将秒杀抢单信息存入到Redis中,这里采用List方式存储,List本身是一个队列  [用户抢单排序 抢单的时候队列弹出 就没有了]
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //用户抢单状态 也需要存一个 -->用于抢单状态查询 上边那个 数据取完就没有了  排队状态存进去
        redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);

        //开启异步 多线程操作
        multiThreadingCreateOrder.createOrder();

        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }
}
