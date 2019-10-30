package com.icicle.goods.service;

import com.github.pagehelper.PageInfo;
import com.icicle.goods.pojo.Brand;

import java.util.List;

/**
 * @Author Max
 * @Date 17:03 2019/8/25
 * @Description：品牌服务接口
 **/
public interface BrandService {

    /**
     * 查询分类对应的品牌集合
     * @param cid 分类id 商品添加时需要
     * @return
     */
    List<Brand> findByCategoryId(Integer cid);

    /**
     * 查询所有品牌
     * @return 品牌集合
     */
    List<Brand> findAll();

    /**
     * 根据id查询
     * @param id 品牌id
     * @return
     */
    Brand findById(Integer id);

    /**
     * 新增品牌
     * @param brand 品牌对象
     */
    void add(Brand brand);

    /**
     * 修改品牌
     * @param brand 品牌对象
     */
    void update(Brand brand);

    /**
     * 根据id删除
     * @param id
     */
    void delete(Integer id);

    /**
     * 多条件搜索品牌的方法
     * @param brand
     * @return
     */
    List<Brand> findList(Brand brand);

    /**
     * 分页查询  使用分页插件  PageInfo是分页插件里边的 里边封装了分页的很多数据 什么都有
     * @param pageNum 当前页码
     * @param size 每页数据条数
     * @return
     */
    PageInfo<Brand> findPage(Integer pageNum,Integer size);


    /**
     * 分页加上条件搜索
     * @param brand 封装的搜索条件
     * @param pageNum 当前页码
     * @param size 每页显示数据条数
     * @return
     */
    PageInfo<Brand> findPage(Brand brand,Integer pageNum,Integer size);


}
