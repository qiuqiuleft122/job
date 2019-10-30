package com.icicle.weChatPay;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Max
 * @Date 10:00 2019/9/11
 * @Description：微信SDK相关测试
 **/
public class WeiXinUtilTest {
    /**
     * 1.生成随机字符串
     * 2.将map转为XML字符串
     * 3.将map转为xml字符串，并带有签名
     * 4.将xml字符串转为map
     */
    @Test
    public void testWeiXinPay() throws Exception {
        //1.生成随机字符串
        String nonceStr = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串：\n"+nonceStr);

        //将map转化为字符串
        Map<String,String> dataMap = new HashMap<>();
        dataMap.put("id","NO.001");
        dataMap.put("title","主机");
        dataMap.put("money","996");
        String xmlStr = WXPayUtil.mapToXml(dataMap);
        System.out.println("XML字符串：\n"+xmlStr);

        //将map转为xml字符串，并且生成签名
        String signedXml = WXPayUtil.generateSignedXml(dataMap, "icicle");
        System.out.println("xml字符串带有签名：\n"+signedXml);

        //将xml字符串转为map
        Map<String, String> map = WXPayUtil.xmlToMap(signedXml);
        System.out.println("xml转成map：\n"+map);
    }
}
