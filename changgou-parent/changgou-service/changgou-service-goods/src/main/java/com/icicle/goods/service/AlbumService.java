package com.icicle.goods.service;

import com.github.pagehelper.PageInfo;
import com.icicle.goods.pojo.Album;

import java.util.List;

/**
 * @Author Max
 * @Date 19:48 2019/8/27
 * @Description：相册业务层接口
 **/

public interface AlbumService {

    /**
     * 查询所有Album
     * @retrun
     */
    List<Album> findAll();

    /**
     * 根据id查询相册
     * @param id 相册id
     * @return
     */
    Album findById(Integer id);

    /**
     * 新增album
     * @param album 新增的相册对象
     */
    void add(Album album);

    /**
     * 修改album
     * @param album album对象
     */
    void update(Album album);

    /**
     * 根据ID删除品牌数据
     * @param id
     */
    void delete(Long id);

    /**
     * 多条件搜索album
     * @param album
     * @return
     */
    List<Album> findList(Album album);

    /**
     * 分页查询
     * @param pageNum 当前页码
     * @param size 每页显示数据条数
     * @return
     */
    PageInfo<Album> findPage(Integer pageNum,Integer size);

    /***
     * Album多条件分页搜索实现
     * @param album 查询条件
     * @param pageNum:当前页
     * @param size:每页显示多少条
     * @return
     */
    PageInfo<Album> findPage(Album album, Integer pageNum, Integer size);
}
