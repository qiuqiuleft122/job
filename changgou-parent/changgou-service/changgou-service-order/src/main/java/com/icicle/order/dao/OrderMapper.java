package com.icicle.order.dao;
import com.icicle.order.pojo.Order;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:Max
 * @Description:Order的Dao
 * @Date 2019/1/28
 *****/
public interface OrderMapper extends Mapper<Order> {
}
