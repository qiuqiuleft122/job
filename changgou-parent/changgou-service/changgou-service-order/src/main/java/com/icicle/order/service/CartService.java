package com.icicle.order.service;

import com.icicle.order.pojo.OrderItem;

import java.util.List;

/**
 * @Author Max
 * @Date 15:35 2019/9/9
 * @Description：购物车接口
 **/
public interface CartService {

    /***
     * 添加购物车实现
     * @param num:购买商品数量
     * @param id：购买ID
     * @param username：购买用户
     * @return
     */
    void add(Integer num, Long id, String username);

    /**
     * 购物车集合查询
     * @param username 用户登录名
     * @return
     */
    List<OrderItem> list(String username);
}
