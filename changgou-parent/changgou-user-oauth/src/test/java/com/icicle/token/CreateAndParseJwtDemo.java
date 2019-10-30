package com.icicle.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Max
 * @Date 19:21 2019/9/6
 * @Description：令牌创建和解析测试
 **/
public class CreateAndParseJwtDemo {

    @Test
    public void testCreateJwt(){
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
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,key_password.toCharArray());

        //从证书中读取秘钥对  参数一：秘钥别名 参数二：秘钥的密码
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypwd.toCharArray());

        //获取私钥 RSA算法   new 这个PrivateKey接口的一个子接口 保证RSA算法
//        PrivateKey privateKey = keyPair.getPrivate();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //创建令牌，需要私钥加盐[RSA算法]  加入一些自定义的载荷
        Map<String,Object> payLoad = new HashMap<>();
        payLoad.put("nikename","tomcat");
        payLoad.put("address","sz");
        payLoad.put("role","admin,user");
        //生成Jwt令牌 参数一：加密的内容  参数二：怎样签名 使用的是一个实现类 rsa算法的
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payLoad), new RsaSigner(privateKey));

        //取出令牌
        String token = jwt.getEncoded();
        System.out.println(token);
    }


    /**
     * 解析令牌
     */
    @Test
    public void TestParseJwt(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJyb2xlIjoiYWRtaW4sdXNlciIsIm5pa2VuYW1lIjoidG9tY2F0In0.eEukzVotwSM6SMIgCxzCMD2bxlGBc1XsdrW31EKsM9Qa8LBoWN3ZAqy2UDiqzNzoYYg9HAR6E5ppf8r0SG2fuK1R-IWLGeJ7NF4PBT6f8oHjl96v9lkIjC81cp-8VTRWV7Me7tUTAWZLajREs3YT0gQzCsAXKw_JCcssq3Kiu5kHUT4DabzyOSyoFpU5icShwcGG-rUuNDPTTNINu52W6yhep1oEgUF-nvWB2mwulBi846GP8bYKxu-xhMscro6hXb6Kww5R4DD0widN7OIiGr-Z8zuIMWpCn6fWyHcvWi4mS6KL3Hl0JKuIQqOmIOSEUqUoDLa5hOhMmIFS81kUjg";
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFsEiaLvij9C1Mz+oyAmt47whAaRkRu/8kePM+X8760UGU0RMwGti6Z9y3LQ0RvK6I0brXmbGB/RsN38PVnhcP8ZfxGUH26kX0RK+tlrxcrG+HkPYOH4XPAL8Q1lu1n9x3tLcIPxq8ZZtuIyKYEmoLKyMsvTviG5flTpDprT25unWgE4md1kthRWXOnfWHATVY7Y/r4obiOL1mS5bEa/iNKotQNnvIAKtjBM4RlIDWMa6dmz+lHtLtqDD2LF1qwoiSIHI75LQZ/CNYaHCfZSxtOydpNKq8eb1/PGiLNolD4La2zf0/1dlcr5mkesV570NxRmU1tFm8Zd3MZlZmyv9QIDAQAB-----END PUBLIC KEY-----";

        //校验JWT原始内容 参数一：令牌 参数二是公钥
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取jwt原始内容  可以判断是谁发的
        String claims = jwt.getClaims();
        System.out.println(claims); //{"address":"sz","role":"admin,user","nikename":"tomcat"}

        //获取令牌
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);  //这个获取的也是令牌 和上边的token一样
    }

}
