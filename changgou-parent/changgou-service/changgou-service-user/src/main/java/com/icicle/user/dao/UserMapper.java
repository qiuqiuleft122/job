package com.icicle.user.dao;
import com.icicle.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:Max
 * @Description:User的Dao
 * @Date 2019/1/28
 *****/
public interface UserMapper extends Mapper<User> {
    /**
     * 添加积分 行级锁保证安全
     * @param username 用户登录名
     * @param points 添加的积分
     */
    @Update("update tb_user set points=points+#{points} where username=#{username}")
    void addPoints(@Param(value = "username") String username,@Param(value = "points") Integer points);
}
