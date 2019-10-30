package com.icicle.goods.dao;
import com.icicle.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:Max
 * @Description:Sku的Dao
 * @Date 2019/1/28
 *****/
public interface SkuMapper extends Mapper<Sku> {
    /**
     * 库存递减 注意 这里需要在传入的参数前加上@Param注解 必须加上
     * @param skuId
     * @param decrNum
     * @return
     */
    @Update("update tb_sku set num=num-#{decrNum} where id=#{skuId} and num>#{decrNum}")
    int decrCount(@Param(value = "skuId") Long skuId, @Param(value = "decrNum") Integer decrNum);
    //@Param 也可以使用索引                      arg0                    arg1
}
