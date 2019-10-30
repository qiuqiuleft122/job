package com.icicle.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayUtil;
import com.icicle.pay.service.WeiXinPayService;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Max
 * @Date 11:05 2019/9/11
 * @Description：微信支付接口实现
 **/
@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    //应用ID
    @Value("${weixin.appid}")
    private String appid;
    //商户号
    @Value("${weixin.partner}")
    private String partner;
    //秘钥
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    //支付回调地址
    @Value("${weixin.notifyurl}")
    private String notifyurl;

    /**
     * 关闭微信支付
     * @param orderId 订单id
     * @return
     */
    @Override
    public Map<String, String> closePay(Long orderId) {
        try {
            //参数 微信支付接口文档提供的关闭订单所需要携带的参数
            Map<String,String> paramMap = new HashMap<>();
            //公众账号ID  配置文件中有 应用ID 注入
            paramMap.put("appid",appid);
            //商户号
            paramMap.put("mch_id",partner);
            //商家订单号
            paramMap.put("out_trade_no",String.valueOf(orderId));
            //随机字符串
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            //Map转成XML字符串，可以携带签名  这里有异常 整个捕获
            String signedXmlParameters = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            //url地址  关闭微信支付订单 接口访问的url
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";
            //创建HttpClient对象
            HttpClient httpClient = new HttpClient(url);
            //设置请求的xml参数
            httpClient.setXmlParam(signedXmlParameters);
            //设置提交方式
            httpClient.setHttps(true);
            //执行提交
            httpClient.post();

            //获取返回数据
            String result = httpClient.getContent();
            return WXPayUtil.xmlToMap(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询微信支付状态
     * @param outtradeno 订单号
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String outtradeno) {
        try {
            //参数 微信支付接口文档提供的查询订单所需要携带的参数
            Map<String,String> paramMap = new HashMap<>();
            //公众账号ID  配置文件中有 应用ID 注入
            paramMap.put("appid",appid);
            //商户号
            paramMap.put("mch_id",partner);
            //订单号
            paramMap.put("out_trade_no",outtradeno);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());

            //Map转成XML字符串，可以携带签名  这里有异常 整个捕获
            String signedXmlParameters = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            //URL地址 查询微信支付订单 接口访问的url
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            //创建HttpClient对象
            HttpClient httpClient = new HttpClient(url);
            //设置请求的xml参数
            httpClient.setXmlParam(signedXmlParameters);
            //设置提交方式
            httpClient.setHttps(true);
            //执行提交
            httpClient.post();

            //获取返回的数据
            String result = httpClient.getContent();
            return WXPayUtil.xmlToMap(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建二维码操作实现
     * @param parameterMap 微信支付接口文档需要携带的请求参数 【主要携带了两个参数：订单号和jine】
     * @return
     */
    @Override
    public Map<String,String> createNative(Map<String, String> parameterMap) {
        try {
            //参数  微信统一下单需要的请求参数  对着微信的统一下单api需要的参数一个个的封装
            Map<String,String> paramMap = new HashMap<>();
            //公众账号ID  配置文件中有 应用ID 注入
            paramMap.put("appid",appid);
            //商户号
            paramMap.put("mch_id",partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //商品描述
            paramMap.put("body","畅购商城商品");
            //商户订单号 传过来
            paramMap.put("out_trade_no",parameterMap.get("outtradeno"));
            //标价金额  单位：分
            paramMap.put("total_fee",parameterMap.get("totalfee"));
            //终端IP
            paramMap.put("spbill_create_ip","127.0.0.1");
            //支付回调地址
            paramMap.put("notify_url",notifyurl);
            //交易类型
            paramMap.put("trade_type","NATIVE");

            //获取自定义数据  [并将其封装到请求参数中]
           // String exchange = parameterMap.get("exchange");  共用一个交换机的话 可以不用传这个参数
            String routingkey = parameterMap.get("routingkey");
            Map<String,String> attachMap = new HashMap<>();
           // attachMap.put("exchange",exchange);
            attachMap.put("routingkey",routingkey);
            //如果是秒杀订单，需要传入username
            String username = parameterMap.get("username");
            //这里需要判断  不为空 就加上 代表是秒杀订单  为空代表普通订单 不需要这个参数
            if (!StringUtils.isEmpty(username)){
                //这里传入username  后边 修改 秒杀订单排队状态 需要用到key=username
                attachMap.put("username",username);
            }

            String attachStr = JSON.toJSONString(attachMap);
            paramMap.put("attach",attachStr);

            //Map转成XML字符串，可以携带签名  这里有异常 整个捕获
            String signedXmlParameters = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            //URL地址  微信支付统一下单接口URL
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            //创建HttpClient对象
            HttpClient httpClient = new HttpClient(url);
            //设置请求的xml参数
            httpClient.setXmlParam(signedXmlParameters);
            //提交方式
            httpClient.setHttps(true);
            //执行请求
            httpClient.post();

            //获取返回的数据 微信那边统一返回xml格式
            String result = httpClient.getContent();
            //将result转成Map 方便看
            return WXPayUtil.xmlToMap(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
