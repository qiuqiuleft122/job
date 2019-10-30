package com.icicle.pay.service;

import java.util.Map;

/**
 * @Author Max
 * @Date 10:58 2019/9/11
 * @Description：微信支付的服务接口
 **/
public interface WeiXinPayService {

    /**
     * 关闭支付
     * @param orderId 订单id
     * @return
     */
    Map<String,String> closePay(Long orderId);

    /**
     * 查询微信支付状态
     * @param outtradeno 订单号
     * @return
     */
    Map<String,String> queryPayStatus(String outtradeno);

    /**
     * 创建二维码操作  模式二 本次选择扫码的方式Native支付
     * @param parameterMap 微信支付接口文档需要携带的请求参数
     */
    Map<String,String> createNative(Map<String,String> parameterMap);
}
