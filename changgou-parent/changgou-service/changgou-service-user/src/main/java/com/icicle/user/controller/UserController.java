package com.icicle.user.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.icicle.user.pojo.User;
import com.icicle.user.service.UserService;
import com.sun.org.apache.regexp.internal.RE;
import entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/****
 * @Author:Max
 * @Description:User的控制层
 * @Date 2019/1/28
 *****/

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /****
     * 添加用户积分
     */
    @GetMapping(value = "/points/add")
    public Result addPoints(Integer points){
        //获取用户名
        String username = TokenDecode.getUserInfo().get("username");
        //调用Service增加积分
        userService.addPoints(username,points);
        return new Result(true,StatusCode.OK,"添加积分成功！");
    }


    /**
     * 用户登录
     * @param username 账号
     * @param password  密码
     * @return
     */
    @GetMapping(value = "/login")
    public Result login(String username,String password,HttpServletResponse response){
        //查询用户的信息  这里的username是主键 可以根据主键查询
        User user = userService.findById(username);

        //对比密码 密码加密-->密文 需要使用BCryptPasswordEncoder的一些工具类 加密后进行对比
        // 使用工具类BCrypt 网上很多  第一个参数 明文  第二个参数 密文
        if (user != null && BCrypt.checkpw(password,user.getPassword())){
            //验证成功 创建令牌信息  并将令牌作为参数 传给用户
            //自定义下信息  不要把秘钥给别人了
            Map<String,Object> tokenMap = new HashMap<>();
            tokenMap.put("role","USER");
            tokenMap.put("success","SUCCESS");
            tokenMap.put("username",username);
            //创建令牌
            String token = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(tokenMap), null);

            //可以将令牌存放在cookie中
            Cookie cookie = new Cookie("Authorization",token);
            cookie.setDomain("localhost");  //表示cookie所属的一个域名
            cookie.setPath("/");
            response.addCookie(cookie); //响应到客户端


            //密码匹配 登录成功 那就将user返回回去 里面有密码也不怕 是密文
            return new Result(true,StatusCode.OK,"登录成功！",token);
        }
        //密码匹配失败 登录失败
        return new Result(false,StatusCode.LOGINERROR,"账号或者密码错误！");
    }

    /***
     * User分页条件搜索实现
     * @param user
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  User user, @PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页条件查询User
        PageInfo<User> pageInfo = userService.findPage(user, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * User分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页查询User
        PageInfo<User> pageInfo = userService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索user数据
     * @param user
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<User>> findList(@RequestBody(required = false)  User user){
        //调用UserService实现条件查询User
        List<User> list = userService.findList(user);
        return new Result<List<User>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除user数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        //调用UserService实现根据主键删除
        userService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改User数据
     * @param user
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  User user,@PathVariable String id){
        //设置主键值
        user.setUsername(id);
        //调用UserService实现修改User
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增User数据
     * @param user
     * @return
     */
    @PostMapping
    public Result add(@RequestBody   User user){
        //调用UserService实现添加User
        userService.add(user);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping({"/{id}","/load/{id}"})
    public Result<User> findById(@PathVariable String id){
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true,StatusCode.OK,"查询成功",user);
    }

    /***
     * 查询User全部数据
     * 只允许管理员才能查询  注意这个引号里边的提示有点慢
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin')")
    @GetMapping
    public Result<List<User>> findAll(){
        //调用UserService实现查询所有User
        List<User> list = userService.findAll();
        return new Result<List<User>>(true, StatusCode.OK,"查询成功",list) ;
    }
}
