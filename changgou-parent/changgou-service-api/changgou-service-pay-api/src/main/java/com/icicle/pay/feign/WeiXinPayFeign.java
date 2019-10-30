package com.icicle.pay.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @Author Max
 * @Date 15:50 2019/9/16
 * @Description：微信支付远程调用feign
 **/
@FeignClient(name = "pay")
@RequestMapping("/weixin/pay")
public interface WeiXinPayFeign {
    /**
     * 关闭支付
     * @param outtradeno 订单编号
     * @return
     */
    @PostMapping(value = "/close")
    Result<Map<String,String>> closePay(Long outtradeno);
}
