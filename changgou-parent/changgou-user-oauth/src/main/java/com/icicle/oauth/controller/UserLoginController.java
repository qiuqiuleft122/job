package com.icicle.oauth.controller;

import com.icicle.oauth.service.UserLoginService;
import com.icicle.oauth.util.AuthToken;
import com.icicle.oauth.util.CookieUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author Max
 * @Date 22:54 2019/9/6
 * @Description：授权认证的控制层
 **/
@RestController
@RequestMapping("/user")
public class UserLoginController {

    //客户端id
    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    //Cookie存储的域名
    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    //Cookie生命周期
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Autowired(required = false)
    private UserLoginService userLoginService;

    /****
     * 登录方法
     * 参数传递：
     * 1.账号		 username=szitheima
     * 2.密码		 password=szitheima
     * 3.授权方式		grant_type=password
     *
     * 请求头传递
     * 4.Basic Base64(客户端ID:客户端秘钥)   Authorization=Basic Y2hhbmdnb3U6Y2hhbmdnb3U=
     */
    @PostMapping(value = "/login")
    public Result login(String username,String password){
        if(StringUtils.isEmpty(username)){
            throw new RuntimeException("用户名不允许为空");
        }
        if(StringUtils.isEmpty(password)){
            throw new RuntimeException("密码不允许为空");
        }
        //调用userLoginService实现登录
        AuthToken authToken = userLoginService.login(username, password, clientId, clientSecret);

        //用户身份令牌
        String access_token = authToken.getAccessToken();
        //将令牌存储到cookie
        saveCookie(access_token);

        return new Result(true, StatusCode.OK,"登录成功",authToken);
    }

    /***
     * 将令牌存储到cookie
     * @param token
     */
    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","Authorization",token,cookieMaxAge,false);
    }
}
