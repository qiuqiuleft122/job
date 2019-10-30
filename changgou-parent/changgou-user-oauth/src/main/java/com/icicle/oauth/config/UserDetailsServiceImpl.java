package com.icicle.oauth.config;
import com.icicle.oauth.util.UserJwt;
import com.icicle.user.feign.UserFeign;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*****
 * 自定义授权认证类
 *
 *
 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * 传入的是username   其实他会走两遍 第一遍走客户端认证，此时的username为客户端ID  ；
     * 第二遍走用户信息和密码登录，此时的username为用户输入的username  【可以Debug去看】
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //==============================客户端信息认证 start=======================
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式
//                return new User(username,  //客户端ID
//                        new BCryptPasswordEncoder().encode(clientSecret),  //客户端秘钥-->加密操作
//                        AuthorityUtils.commaSeparatedStringToAuthorityList("")); //权限
                //数据库查找方式 【注意这里返回的是安全框架里边的User 自己会去校验密码和账号是否正确】
                return new User(username,
                        clientSecret,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        //==============================客户端信息认证 end=======================

        //==============================用户账号密码信息认证 start=======================
        if (StringUtils.isEmpty(username)) {  //这个时候的username为用户输入的用户名
            return null;
        }

        /***
         * 从数据库加载查询用户信息
         * 1:没有令牌，Feign调用之前，生成令牌(admin)
         * 2:Feign调用之前，令牌需要携带过去
         * 3:Feign调用之前，令牌需要存放到Header文件中
         * 4:请求->Feign调用->拦截器RequestInterceptor->Feign调用之前执行拦截
         */
        Result<com.icicle.user.pojo.User> userResult = userFeign.findById(username);


        //客户端ID     :  changgou
        //客户端秘钥   :  changgou
        //普通用户->账号：任意账号   密码：szitheima
        if (userResult == null || userResult.getData() == null){
            return null;
        }

//        String pwd = new BCryptPasswordEncoder().encode("szitheima");
        String pwd = userResult.getData().getPassword();
        //创建User对象
        String permissions = "salesman,accountant,user";  //指定用户的角色信息->数据库中设计 也是从数据库中去查找
        UserJwt userDetails = new UserJwt(username,pwd,AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));
        // =============用户账号密码信息认证 end=======================
        return userDetails;
    }
}
