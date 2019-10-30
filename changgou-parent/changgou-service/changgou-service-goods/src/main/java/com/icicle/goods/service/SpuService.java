package com.icicle.goods.service;

import com.github.pagehelper.PageInfo;
import com.icicle.goods.pojo.Goods;
import com.icicle.goods.pojo.Spu;

import java.util.List;

/****
 * @Author:Max
 * @Description:Spu业务层接口
 * @Date 2019/1/28
 *****/
public interface SpuService {

    /***
     * 还原被删除商品
     * @param spuId
     */
    void restore(Long spuId);

    /**
     * 逻辑删除
     * @param spuId 需要删除的商品id
     */
    void logicDelete(Long spuId);

    /**
     * 批量下架
     * @param spuIds 需要下架的所有商品ID
     * @return 返回下架的商品数量
     */
    int pullMany(Long[] spuIds);

    /**
     * 批量上架
     * @param spuIds 前端传递一组商品ID
     * @return 返回批量上架的商品数量
     */
    int putMany(Long[] spuIds);

    /**
     * 商品上架
     * @param spuId 公共属性的spuId
     */
    void put(Long spuId);

    /**
     * 商品下架
     * @param spuId 公共属性的spuId
     */
    void pull(Long spuId);

    /**
     * 商品审核
     * @param spuId 公共属性的spuId
     */
    void audit(Long spuId);

    /**
     * 根据SPU的ID查找SPU以及对应的SKU集合
     * @param spuId 公共属性spuId
     * @return
     */
    Goods findGoodsById(Long spuId);

    /**
     * 保存商品
     * @param goods 新增的商品信息
     */
    void saveGoods(Goods goods);

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu  物理删除
     * @param spuId
     */
    void delete(Long spuId);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();
}
