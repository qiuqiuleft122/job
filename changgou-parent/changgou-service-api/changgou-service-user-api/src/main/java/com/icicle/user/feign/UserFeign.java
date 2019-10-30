package com.icicle.user.feign;

import com.icicle.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author Max
 * @Date 16:33 2019/9/8
 * @Description：用户微服务中提供用户信息查询的方法，
 * 并在oauth中使用feign调用即可
 **/
@FeignClient(name = "user")
@RequestMapping("/user")
public interface UserFeign {

    /**
     * 根据用户id查询用户信息
     * @param id
     * @return
     */
    @GetMapping(value = "/load/{id}")
    Result<User> findById(@PathVariable String id);

    /****
     * 添加用户积分  注意feign的调用 需要传递过来的参数一定的加上一些接收参数相关的注解
     * @param points
     * @return
     */
    @GetMapping(value = "/points/add")
    Result addPoints(@RequestParam Integer points);

}
