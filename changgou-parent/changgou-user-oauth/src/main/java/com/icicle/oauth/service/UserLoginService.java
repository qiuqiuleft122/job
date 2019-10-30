package com.icicle.oauth.service;

import com.icicle.oauth.util.AuthToken;

/**
 * @Author Max
 * @Date 21:49 2019/9/6
 * @Description：授权认证的方法
 **/
public interface UserLoginService {

    /**
     * 授权认证方法
     * @param username 登录账号
     * @param password 登录密码
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return 返回封装的令牌对象
     */
    AuthToken login(String username,String password,String clientId,String clientSecret);
}
