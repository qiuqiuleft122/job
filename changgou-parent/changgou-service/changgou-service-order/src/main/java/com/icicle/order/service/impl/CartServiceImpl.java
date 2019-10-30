package com.icicle.order.service.impl;

import com.icicle.goods.feign.SkuFeign;
import com.icicle.goods.feign.SpuFeign;
import com.icicle.goods.pojo.Sku;
import com.icicle.goods.pojo.Spu;
import com.icicle.order.pojo.OrderItem;
import com.icicle.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Max
 * @Date 15:36 2019/9/9
 * @Description：购物车实现
 **/
@Service
public class CartServiceImpl implements CartService {

    //指定购物车数据存放在哪里
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    /**
     * 加入购物车实现
     * @param num:购买商品数量
     * @param id：购买ID skuId
     * @param username：购买用户
     */
    @Override
    public void add(Integer num, Long id, String username) {
        //当添加购物车数量<=0的时候，需要移除该商品信息
        if (num <= 0){
            //移除该购物车商品
            redisTemplate.boundHashOps("Cart_"+username).delete(id);

            //如果此时购物车数量为空，则连购物车一起移除
            Long size = redisTemplate.boundHashOps("Cart_"+username).size();
            if (size == null || size <= 0){
                redisTemplate.delete("Cart_"+username);
            }
            return;
        }
        //查询商品的详情
        //1.查询sku
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        //2.查询spu
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();

        //创建一个orderItem对象 用于接收加入购物车的商品对象
        OrderItem orderItem = createOrderItem(num, sku, spu);

        //将购物车数据存入到redis：namespace->username
        redisTemplate.boundHashOps("Cart_"+username).put(id,orderItem);
    }

    /**
     * 购物车集合查询
     * @param username 用户登录名
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        //获取指定命名空间下所有数据
        return redisTemplate.boundHashOps("Cart_"+username).values();
    }

    /**
     * 创建一个orderItem对象
     * @param num
     * @param sku
     * @param spu
     * @return
     */
    private OrderItem createOrderItem(Integer num, Sku sku, Spu spu) {
        //将加入购物车的商品信息封装成OrderItem
        OrderItem orderItem = new OrderItem();

        //分类的id
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());

        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setNum(num);
        orderItem.setPrice(sku.getPrice());
        orderItem.setMoney(num*orderItem.getPrice());  //单价*数量
        orderItem.setImage(sku.getImage());
        return orderItem;
    }


}
