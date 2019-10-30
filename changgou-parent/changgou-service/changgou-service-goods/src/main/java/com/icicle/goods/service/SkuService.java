package com.icicle.goods.service;

import com.github.pagehelper.PageInfo;
import com.icicle.goods.pojo.Sku;

import java.util.List;
import java.util.Map;

/****
 * @Author:Max
 * @Description:Sku业务层接口
 * @Date 2019/1/28
 *****/
public interface SkuService {

    /**
     * 商品递减
     * @param decrMap
     */
    void decrCount(Map<String,Integer> decrMap);

    /**
     * 根据状态查询SKU列表  【只有审核通过的才能被找到】
     * @param status 状态
     * @return
     */
    List<Sku> findByStatus(String status);

    /***
     * Sku多条件分页查询
     * @param sku
     * @param page
     * @param size
     * @return
     */
    PageInfo<Sku> findPage(Sku sku, int page, int size);

    /***
     * Sku分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Sku> findPage(int page, int size);

    /***
     * Sku多条件搜索方法
     * @param sku
     * @return
     */
    List<Sku> findList(Sku sku);

    /***
     * 删除Sku
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Sku数据
     * @param sku
     */
    void update(Sku sku);

    /***
     * 新增Sku
     * @param sku
     */
    void add(Sku sku);

    /**
     * 根据ID查询Sku
     * @param id
     * @return
     */
     Sku findById(Long id);

    /***
     * 查询所有Sku
     * @return
     */
    List<Sku> findAll();
}
