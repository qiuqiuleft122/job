package com.icicle.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author Max
 * @Date 16:51 2019/9/5
 * @Description：自定义全局过滤器 处理登录
 **/
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //定义一个 令牌的名字 用于取出令牌
    private static final String AUTHORIZE_TOKEN = "Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //判断 如果是请求登录 或者查找品牌 是不能拦截的 放行
        //如果是登录、goods等开放的微服务[这里的goods部分开放],则直接放行,这里不做完整演示，完整演示需要设计一套权限系统
//        if (path.startsWith("/api/user/login") || path.startsWith("api/brand/search")){
//            return chain.filter(exchange);
//        }
        //获取请求的url  单独写一个类URLFilter进行过滤操作
        String uri = request.getURI().toString();
        if (URLFilter.hasAuthorize(uri)){
            //代表可以过滤 这些路径不需要校验  放行
            return chain.filter(exchange);
        }

        //令牌存在位置：参数中 、 头文件中 、cookie中 传入令牌的方式都有可能  实际开发规定：优先从头  接着参数  最后cooKie
        //获取头文件中的令牌信息  获取请求头中第一个参数
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

        //设置一个boolean值  用于判断token是否在头文件中
        boolean flag = true;

        //参数中获取
        if (StringUtils.isEmpty(token)){
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            //这里获取的话那么token就不在头文件中  下边的cookie中获取也是一样了 false 不用再设置了
            flag = false;
        }

        //cookie中获取
        if (StringUtils.isEmpty(token)){
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);  //注意这里返回的是一个cookie对象
            //需要判断cookie是否为空
            if (httpCookie != null){
                //不为空 才给token赋值
                token = httpCookie.getValue();
            }
        }

        //如果没有令牌 则拦截
        if (StringUtils.isEmpty(token)){
            //设置方法不允许被访问，401无权限  405错误代码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据  前后端分离 响应状态码给前端 注意了
            return response.setComplete();
        }

        //如果有令牌就校验 则校验令牌是否有效  有效  走到这里就代表有令牌 那么需要解析  这里不需要校验了 框架帮我们校验了
//        try {
//            JwtUtil.parseJWT(token);
//        } catch (Exception e) {
//           //无效拦截  设置状态码 401
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            //响应空数据  前后端分离 响应状态码给前端 注意了
//            return response.setComplete();
//        }

        //放行之前 判断flag的值  将令牌封装到头文件中  为auth2.0做准备
        if (!flag){
            //判断当前令牌是否有bearer前缀，如果没有，则添加前缀 bearer  [这个非常重要 必须加上]
            if(!token.startsWith("bearer ") && !token.startsWith("Bearer ")){
                token="bearer "+token;
            }
            //代表flag为false 那么需要封装到头文件中
            //System.out.println(token);
            request.mutate().header(AUTHORIZE_TOKEN,token);
        }

        //有效 放行
        return chain.filter(exchange);
    }

    /**
     * 过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
