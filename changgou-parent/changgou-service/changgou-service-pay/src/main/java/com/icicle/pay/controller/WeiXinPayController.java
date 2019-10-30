package com.icicle.pay.controller;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayUtil;
import com.icicle.pay.service.WeiXinPayService;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @Author Max
 * @Date 11:42 2019/9/11
 * @Description：
 **/
@RestController
@RequestMapping("/weixin/pay")
public class WeiXinPayController {

    @Autowired
    private WeiXinPayService weiXinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //队列交换机信息注入
    @Value("${mq.pay.exchange.order}")
    private String exchange;

    @Value("${mq.pay.queue.order}")
    private String queue;

    @Value("${mq.pay.routing.key}")
    private String routing;

    /**
     * 支付结果通知回调的方法  【微信支付那边调用的】
     *
     * @param request 请求
     * @return
     */
    @GetMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request) {
        try {
            //注意网络中的通信 数据的传递 都是流的形式
            //获取网络输入流
            ServletInputStream is = request.getInputStream();
            //创建一个OutputStream->输入文件中  直接变成字节数组输出流  这样更加方便转成 字符串
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //操作流的读取
            byte[] buffer = new byte[1024];
            //定义一个读取变量
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);//读取有效长度
            }
            //关闭资源
            baos.flush();
            baos.close();
            is.close();
            //微信支付结果的字节数据
            String resultXml = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            //将xml数据换成Map数据
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            System.out.println(resultMap); //打印支付结果看看

            Map<String,String> attach = JSON.parseObject(resultMap.get("attach"), Map.class);
            //发送支付结果给MQ  注入MQ的一些信息 .convertAndSend()
            // 参数一：交换机名字  参数二：路由的key  参数三：消息内容(Json字符串形式)
            rabbitTemplate.convertAndSend(exchange,attach.get("routingkey"), JSON.toJSONString(resultMap));

            //响应 接收应答数据给微信  [格式是微信这边规定的  必须这样]
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        } catch (Exception e) {
            e.printStackTrace();
            //记录错误日志
        }
        return null;
    }

    /**
     * 关闭支付
     * @param outtradeno 订单编号
     * @return
     */
    @PostMapping(value = "/close")
    public Result<Map<String,String>> closePay(Long outtradeno){
        //关闭支付
        Map<String, String> resultMap = weiXinPayService.closePay(outtradeno);
        return new Result<>(true,StatusCode.OK,"关闭支付成功！",resultMap);
    }

    /**
     * 微信支付状态查询
     *
     * @param outtradeno
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result<Map<String, String>> queryPayStatus(String outtradeno) {
        //查询支付状态
        Map<String, String> resultMap = weiXinPayService.queryPayStatus(outtradeno);
        return new Result<>(true, StatusCode.OK, "查询支付状态成功", resultMap);
    }

    /**
     * 创建二维码操作
     *
     * @param parameterMap 需要携带两个参数 一个是订单号 一个金额 为什么是Map呢？map是万能的
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result<Map<String, String>> createNative(@RequestParam Map<String, String> parameterMap) {
        //创建二维码
        Map<String, String> resultMap = weiXinPayService.createNative(parameterMap);
        return new Result<>(true, StatusCode.OK, "创建二维码预付订单成功", resultMap);
    }
}
