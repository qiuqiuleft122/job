package com.icicle.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Max
 * @Date 17:12 2019/9/9
 * @Description：管理员令牌发放
 **/
public class AdminToken {

    public static String adminToken(){
        //证书文件路径
        String key_location = "changgou.jks";
        //秘钥库密码  -storepass 对应的值 创建秘钥工厂的时候使用
        String key_password = "changgou";
        //秘钥密码 -keypass 对应的值  读取秘钥对的时候使用
        String keypwd = "changgou";
        //秘钥别名
        String alias = "changgou";

        //加载证书  读取类路径下的文件
        ClassPathResource resource = new ClassPathResource(key_location);

        //加载读取证书数据 创建秘钥工厂进行读取 这个时候使用 参数一：证书资源  参数二：秘钥库密码
        org.springframework.security.rsa.crypto.KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,key_password.toCharArray());

        //从证书中读取秘钥对  参数一：秘钥别名 参数二：秘钥的密码
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypwd.toCharArray());

        //获取私钥 RSA算法   new 这个PrivateKey接口的一个子接口 保证RSA算法
//        PrivateKey privateKey = keyPair.getPrivate();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //创建令牌，需要私钥加盐[RSA算法]  加入一些自定义的载荷
        Map<String,Object> payLoad = new HashMap<>();
        //此时这个地方是根据之前写死的权限生成的格式写的 这个authorities是固定的 格式为：key：value为string数组格式
        payLoad.put("authorities",new String[]{"admin","oauth"});
        //生成Jwt令牌 参数一：加密的内容  参数二：怎样签名 使用的是一个实现类 rsa算法的
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payLoad), new RsaSigner(privateKey));

        //获取令牌数据
        return jwt.getEncoded();
    }
}
