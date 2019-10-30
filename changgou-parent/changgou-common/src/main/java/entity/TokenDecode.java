package entity;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Max
 * @Date 20:39 2019/9/9
 * @Description：用于解密令牌信息，在类中读取公钥信息
 **/
@Component
public class TokenDecode {

    //公钥
    private static final String PUBLIC_KEY = "public.key";

    private static String publickey="";


    /**
     * 获取非对称加密公钥 Key
     * @return 公钥 Key
     */
    public static String getPubKey() {
        //这个地方的含义，在于： 精髓在于只加载一次 不用每次都去读取公钥 类似静态代码块的 【很好】
        // 如果公钥不为空 那么就直接返回公钥 就不用加载去读取
        //如果公钥为空 那么就需要走下边 流获取了  而且获取一次就去给静态成员变量赋值 publickey就不为空了 下次就可以直接反回了 很优秀的设计
        if(!StringUtils.isEmpty(publickey)){
            return publickey;
        }
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            publickey = br.lines().collect(Collectors.joining("\n"));
            return publickey;
        } catch (IOException ioe) {
            return null;
        }
    }

    /***
     * 读取令牌数据
     */
    public static Map<String,String> dcodeToken(String token){
        //校验Jwt   其实这里只是单纯的为了拿到jwt 成功才可以拿到jwt 顺带校验了
        // 因为 config里边的ResourceServerConfig这个类上边的
        // @EnableResourceServer//开启资源校验服务 -->校验令牌真伪  会优先校验真伪了
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(getPubKey()));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        return JSON.parseObject(claims,Map.class);
    }

    /***
     * 从容器中获取令牌信息，包含了用户信息
     * @return
     */
    public static Map<String,String> getUserInfo(){
        //获取授权信息
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        //令牌解码
        return dcodeToken(details.getTokenValue());
    }
}

