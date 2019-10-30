package com.icicle.user.service;

import com.github.pagehelper.PageInfo;
import com.icicle.user.pojo.User;

import java.util.List;

/****
 * @Author:Max
 * @Description:User业务层接口
 * @Date 2019/1/28
 *****/
public interface UserService {

    /**
     * 添加积分
     * @param username 给登录的用户添加积分
     * @param points 需要加的积分
     */
    void addPoints(String username, Integer points);

    /***
     * User多条件分页查询
     * @param user
     * @param page
     * @param size
     * @return
     */
    PageInfo<User> findPage(User user, int page, int size);

    /***
     * User分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<User> findPage(int page, int size);

    /***
     * User多条件搜索方法
     * @param user
     * @return
     */
    List<User> findList(User user);

    /***
     * 删除User
     * @param id
     */
    void delete(String id);

    /***
     * 修改User数据
     * @param user
     */
    void update(User user);

    /***
     * 新增User
     * @param user
     */
    void add(User user);

    /**
     * 根据ID查询User
     * @param id
     * @return
     */
     User findById(String id);

    /***
     * 查询所有User
     * @return
     */
    List<User> findAll();
}
