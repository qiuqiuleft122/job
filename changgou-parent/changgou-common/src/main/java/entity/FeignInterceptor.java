package entity;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * @Author Max
 * @Date 17:39 2019/9/9
 * @Description：拦截器控制 订单服务远程调用goods微服务所用
 * 获取用户的令牌
 * 将令牌再封装到头文件中
 **/
//@Configuration  作为工具类 就不需要这个注解了 只需要到需要的服务的清泪里边注入@Bean构造方法这个注解即可
public class FeignInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前，进行拦截  此时就不能使用admin令牌了 权限太大 应该是某个用户 用户的令牌
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        try {
            //requestAttributes这个里边记录了当前用户请求的所有数据，包含请求头和请求参数等

            //用户当前请求的时候对应线程的数据,如果开启了熔断，默认是线程池隔离，会开启新的线程，需要将熔断策略换成信号量隔离，此时不会开启新的线程
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //判断记录的数据是否为空
            if (requestAttributes != null) {
                /**
                 * 获取请求头中的数据
                 * 获取所有请求头中的名字
                 */
                Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
                //遍历所有的请求头
                while (headerNames.hasMoreElements()) {
                    //请求头的key
                    String headerKey = headerNames.nextElement();
                    //获取请求头的值
                    String headerValue = requestAttributes.getRequest().getHeader(headerKey);
                    //将请求头信息封装到头中，使用Feign调用的时候，会传递给下一个微服务
                    System.out.println(headerKey + ":" + headerValue);
                    template.header(headerKey, headerValue);
                }
            }
        } catch (Exception e) {
            //异常
            e.printStackTrace();
        }
    }
}
