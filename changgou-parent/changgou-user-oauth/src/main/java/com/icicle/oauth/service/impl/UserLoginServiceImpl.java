package com.icicle.oauth.service.impl;

import com.icicle.oauth.service.UserLoginService;
import com.icicle.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @Author Max
 * @Date 21:57 2019/9/6
 * @Description：授权方法的实现类
 **/
@Service
public class UserLoginServiceImpl implements UserLoginService {

    //获取当前服务的uri
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 授权认证的实现
     * @param username 登录账号
     * @param password 登录密码
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        AuthToken authToken = applyToken(username,password,clientId,clientSecret);
        if (authToken == null){
            throw new RuntimeException("申请令牌失败");
        }
        return authToken;
    }

    /**
     * 提取出来的获取令牌封装对象的公共方法  [主要目的模拟使用客户端id和密码登录这一步的操作]
     * @param username 登录账号
     * @param password 登录密码
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //选中认证的服务地址  参数为 当前服务的serviceId 配置文件汇总  获取指定服务的注册数据
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        if (serviceInstance == null){
            throw new RuntimeException("找不到对应的服务");
        }
        //获取令牌的url  调用的请求地址 http://localhost:9001/oauth/token  但是不要写死了 造成硬编码
        String path = serviceInstance.getUri() + "/oauth/token";

        //请求提交的数据封装
        MultiValueMap<String,String> parameterMap = new LinkedMultiValueMap<>();
        //账号
        parameterMap.add("username",username);
        //密码
        parameterMap.add("password",password);
        //授权方式
        parameterMap.add("grant_type","password");

        //请求头封装  因为那个框架本身登录界面客户端 遵循http Basic认证  所以需要封装一个授权的请求头
        MultiValueMap<String,String> header = new LinkedMultiValueMap<>();
        //封装的编码方式
        header.add("Authorization",httpBasic(clientId,clientSecret));
        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override   //自己去DefaultResponseErrorHandler 复制重写的方法
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        //用户登录后的令牌信息 在map里
        Map map = null;
        try {
            //HttpEntity->创建该对象 封装了请求头和请求体
            HttpEntity httpEntity = new HttpEntity(parameterMap,header);
            /****
             * 1:请求地址
             * 2:提交方式
             * 3:requestEntity:请求提交的数据信息封装 请求体|请求头
             * 4:responseType:返回数据需要转换的类型
             */
            ResponseEntity<Map> response = restTemplate.exchange(path, HttpMethod.POST, httpEntity, Map.class);
            //取出有令牌信息的map
            map = response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
        //进行判断 更加严谨
        if(map == null || map.get("access_token") == null || map.get("refresh_token") == null || map.get("jti") == null){
            //jti是jwt令牌的唯一标识作为用户身份令牌
            throw new RuntimeException("创建令牌失败！");
        }
        //将相应的数据封装成AuthToken
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(map.get("access_token").toString());
        authToken.setRefreshToken(map.get("refresh_token").toString());
        authToken.setJti(map.get("jti").toString());
        return authToken;
    }

    /**
     * 公共base64编码实现http basic的认证   注意Basic后边有个空格
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return
     */
    private String httpBasic(String clientId, String clientSecret){
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String str = clientId+":"+clientSecret;
        //进行base64编码
        byte[] bytes = Base64.getEncoder().encode(str.getBytes());
        return "Basic "+new String(bytes,StandardCharsets.UTF_8);
    }
}
