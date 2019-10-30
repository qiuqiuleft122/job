package com.icicle.jjwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @Author Max
 * @Date 15:32 2019/9/5
 * @Description：测试JWT令牌
 **/
public class JWTTest {

    /**
     * 创建Jwt令牌
     */
    @Test
    public void testCreateJWT(){
        JwtBuilder builder = Jwts.builder();
        builder.setId("888");  //设置唯一编号
        builder.setIssuer("之禾");  //设置颁发者
        builder.setIssuedAt(new Date());  //设置颁发时间
        builder.setExpiration(new Date(System.currentTimeMillis()+35000));  //设置过期时间  当前系统时间 + 25秒
        builder.setSubject("测试专用");  //设置主题  可以是JSON数据
        builder.signWith(SignatureAlgorithm.HS256,"icicle"); //1.是签名算法 2.秘钥（盐）
        // 设置签名 使用HS256算法，并设置SecretKey(字符串)

        //自定义数据：
        Map<String,Object> userInfo = new HashMap<>();
        userInfo.put("company","qq");
        userInfo.put("tag","cool");
        userInfo.put("account","2500000");
        builder.addClaims(userInfo);  //这个方法 默认加map类型的

        System.out.println(builder.compact()); //输出这个密文
    }

    /**
     * 解析Jwt令牌
     */
    @Test
    public void testParseJWT(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJpc3MiOiLkuYvnpr4iLCJpYXQiOjE1Njc2NzA3NTEsImV4cCI6MTU2NzY3MDc4Niwic3ViIjoi5rWL6K-V5LiT55SoIiwiY29tcGFueSI6InFxIiwidGFnIjoiY29vbCIsImFjY291bnQiOiIyNTAwMDAwIn0.bQ86UmKRWyvave8VtDx00JdA2mkFEnH5v3p-tftxmyM";
        Claims claims = Jwts.parser()
                .setSigningKey("icicle")  //需要知道秘钥 进行解密
                .parseClaimsJws(token)  //需要解析的令牌对象
                .getBody();  //获取解析之后的结果
        System.out.println(claims);
    }


}
