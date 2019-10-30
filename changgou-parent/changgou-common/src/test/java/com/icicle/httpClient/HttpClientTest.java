package com.icicle.httpClient;

import entity.HttpClient;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author Max
 * @Date 10:33 2019/9/11
 * @Description：HttpClient使用案例
 **/
public class HttpClientTest {

    /**
     * 发送Http/https请求
     *  发送指定参数
     *  可以获取响应的结果
     */
    @Test
    public void testHttpClient() throws IOException {
        //微信统一下单的接口URL地址：https://api.mch.weixin.qq.com/pay/unifiedorder
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        //操作步骤
        //1.创建HttpClient对象
        HttpClient httpClient = new HttpClient(url);
        //2.要发送的xml数据 接口文档要求 必须传xml数据 ->post请求
        String xmlStr = "<xml><name>刘德华</name></xml>";
        //3.设置请求的url参数
        httpClient.setXmlParam(xmlStr);
        //4.设置协议
        httpClient.setHttps(true);
        //5.发送请求 post
        httpClient.post();
        //6.获取响应数据
        String result = httpClient.getContent();
        System.out.println("响应数据：\n " + result);
    }
}
