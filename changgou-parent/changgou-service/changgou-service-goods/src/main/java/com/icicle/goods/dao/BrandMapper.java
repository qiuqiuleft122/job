package com.icicle.goods.dao;

import com.icicle.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author Max
 * @Date 16:49 2019/8/25
 * @Description：Dao层:
 *  使用通用Mapper->MyBatis动态SQL
 *  SQL语句,面向对象操作
 *  要求：Dao必须集成Mapper<T>接口  tk.mybatis包下边的
 *        Mapper接口中有增删改查各种操作
 **/
public interface BrandMapper extends Mapper<Brand> {

    /**
     * 查询分类对应的品牌集合
     * @param cid
     * @return
     */
    @Select("SELECT tb.* FROM tb_category_brand tcb,tb_brand tb WHERE tcb.brand_id = tb.id AND tcb.category_id=#{cid}")
    List<Brand> findByCategoryId(Integer cid); //记住Mapper这个类是tk.mybatis下边的


}
