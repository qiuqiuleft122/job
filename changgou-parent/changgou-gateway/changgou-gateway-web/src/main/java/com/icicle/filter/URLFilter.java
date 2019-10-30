package com.icicle.filter;

/**
 * @Author Max
 * @Date 14:37 2019/9/8
 * @Description：不需要认证就能访问的路径校验
 **/
public class URLFilter {
    //定义那些路径不需要认证就能访问
    private static final String allUrl = "/user/login,/api/user/add";

    /****
     * 校验当前访问路径是否需要验证权限
     * 如果不需要验证：true
     * 如果需要验证：false
     * @param url
     * @return
     */
    public static boolean hasAuthorize(String url){
        //不需要拦截的url
        String[] urls = allUrl.split(",");
        for (String uri : urls) {
            if (uri.equals(url)){
                return true;
            }
        }
        return false;
    }
}
