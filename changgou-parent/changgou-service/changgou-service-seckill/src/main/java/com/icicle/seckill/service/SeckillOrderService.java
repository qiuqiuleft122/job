package com.icicle.seckill.service;

import com.github.pagehelper.PageInfo;
import com.icicle.seckill.pojo.SeckillOrder;
import entity.SeckillStatus;

import java.util.List;

/****
 * @Author:Max
 * @Description:SeckillOrder业务层接口
 * @Date 2019/1/28
 *****/
public interface SeckillOrderService {

    /**
     * 关闭订单 回滚库存
     * @param username 用户名
     */
    void deleteOrder(String username);

    /**
     * 更新redis中的订单状态
     * @param username 登录的用户名
     * @param transactionId 交易的流水号
     * @param endTime 支付时间
     */
    void updatePayStatus(String username,String transactionId,String endTime);

    /**
     *  查询某个用户的排队状态
     * @param username 登录的用户
     * @return
     */
    SeckillStatus queryStatus(String username);

    /**
     * 添加秒杀订单
     * @param id 商品id
     * @param time 商品秒杀开始时间
     * @param username 用户登录名
     * @return
     */
    Boolean add(Long id,String time, String username);

    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
     SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();
}
