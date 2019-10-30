package com.icicle.seckill.timer;

import com.icicle.seckill.dao.SeckillGoodsMapper;
import com.icicle.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author Max
 * @Date 17:18 2019/9/12
 * @Description：定时将符合参与秒杀的商品查询出来再存入到Redis缓存
 **/
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 每10秒执行一次  定时操作
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void loadGoodsPushRedis(){
        /****
         * 1.查询复合当前参与秒杀的时间菜单
         * 2.秒杀商品库存>0 stock_count
         * 3.审核状态->审核通过  status:1
         * 4.开始时间 start_time,结束时间end_time
         *   时间菜单的开始时间=<start_time   && end_time<时间菜单的开始时间+2小时
         */

        //获取时间段集合（时间菜单）
        List<Date> dateMenus = DateUtil.getDateMenus();
        //循环查询每个时间区间的秒杀商品
        for (Date startTime : dateMenus) {
            // namespace = SeckillGoods_20195712  redis中的存储方式 采用hash类型  时间段命名空间  商品id为key 商品详情为value
            //时间的字符串格式yyyyHHddHH
            String extName = DateUtil.data2str(startTime, DateUtil.PATTERN_YYYYMMDDHH);
            String timespace = "SeckillGoods_"+extName;

            //根据时间段查询对应的秒杀商品数据  这里需要组装条件
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //1.审核状态->审核通过  status:1
            criteria.andEqualTo("status","1");
            //2.秒杀商品库存>0 stock_count
            criteria.andGreaterThan("stockCount",0);
            //时间菜单的开始时间=<start_time   && end_time<时间菜单的开始时间+2小时
            criteria.andGreaterThanOrEqualTo("startTime",startTime);
            criteria.andLessThan("endTime",DateUtil.addDateHour(startTime,2));

            //因为是每五秒 执行一次 可能会有相同商品不断存入 虽然不会重复 也没有必要
            //排除已经存入到了Redis中的SeckillGoods->1)求出当前命名空间下所有的商品的ID(key)  2)每次查询排除掉之前存在的商品的key的数据
            Set keys = redisTemplate.boundHashOps(timespace).keys();
            if (keys != null && keys.size() >0 ){
                //排除
                criteria.andNotIn("id",keys);
            }

            //查询数据
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

            //循环存入到redis
            for (SeckillGoods seckillGood : seckillGoods) {
                System.out.println("商品ID:"+seckillGood.getId()+"---存入到了Redis--"+timespace);
                //存入redis
                redisTemplate.boundHashOps(timespace).put(seckillGood.getId(),seckillGood);
                //商品数据队列存储,防止高并发超卖 给每个商品做个队列
                Long[] ids = pushIds(seckillGood.getStockCount(), seckillGood.getId());
                //redis队列中有一个可以一次性添加好多个的
                redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGood.getId()).leftPushAll(ids);
                //自增计数器  秒杀商品的初始库存
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGood.getId(),seckillGood.getStockCount());
            }

        }
    }

    /**
     * 获取每个商品的ID集合 【就是这个商品有几个】
     * @param num 某个秒杀商品的库存
     * @param id 商品id
     * @return
     */
    private Long[] pushIds(Integer num,Long id){
        Long[] ids = new Long[num];
        for (int i = 0; i < ids.length; i++) {
           ids[i] = id;
        }
        return ids;
    }
}
