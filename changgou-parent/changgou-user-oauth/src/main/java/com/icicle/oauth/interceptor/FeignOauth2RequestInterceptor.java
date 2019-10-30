package com.icicle.oauth.interceptor;

import com.icicle.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Max
 * @Date 17:39 2019/9/9
 * @Description：拦截器控制 oauth需要远程调用用户微服务去查询 之前是放行 现在是给令牌
 **/
@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前，进行拦截  去调用用户微服务的 之前加入一些头信息管理员令牌
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        /***
         * 从数据库加载查询用户信息
         * 1:没有令牌，Feign调用之前，生成令牌(admin)
         * 2:Feign调用之前，令牌需要携带过去
         * 3:Feign调用之前，令牌需要存放到Header文件中
         * 4:请求->Feign调用->拦截器RequestInterceptor->Feign调用之前执行拦截
         */

        //生成admin令牌
        String adminToken = AdminToken.adminToken();
        template.header("Authorization","bearer "+adminToken);
    }
}
