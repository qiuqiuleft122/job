package com.icicle.search.controller;

import com.icicle.search.feign.SkuFeign;
import com.icicle.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author Max
 * @Date 17:23 2019/9/3
 * @Description：实现搜索调用控制层
 **/
@Controller
@RequestMapping("/search")
public class SkuController {

    @Autowired(required = false)
    private SkuFeign skuFeign;

    /**
     * 实现搜索调用
     * @param searchMap
     * @param model
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false)Map<String,String> searchMap,Model model){
        //替换特殊字符 处理特殊字符
        handleSearchMap(searchMap);
        //调用搜索微服务
        Map<String, Object> resultMap = skuFeign.search(searchMap);
        //搜索数据结果
        model.addAttribute("result",resultMap);
        //搜索条件 回显
        model.addAttribute("searchMap",searchMap);

        //计算分页
        Page<SkuInfo> pageInfo = new Page<>(
                Long.parseLong(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNum").toString())+1, //注意这里默认得到的pageNum是从0开始的
                Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo",pageInfo);

        //请求地址解析
        String[] urls = url(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sortUrl",urls[1]);  //排序路径  每次切换排序不需要之前的排序信息
        return "search";
    }


    /****
     * 拼接组装用户请求的URL地址
     * 获取用户每次请求的地址
     * 页面需要在这次请求的地址上面添加额外的搜的条件
     * http://localhost:18086/search/list
     * http://localhost:18086/search/list?keywords=华为
     * http://localhost:18086/search/list?keywords=华为&brand=华为
     * http://localhost:18086/search/list?keywords=华为&brand=华为&category=语言文字
     */
    private String[] url(Map<String,String> searchMap){
        String url = "/search/list"; //初始化地址
        String sortUrl = "/search/list";
        if (searchMap != null && searchMap.size() >0){
            url+="?";
            sortUrl+= "?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //key是搜索的条件对象
                String key = entry.getKey();

                //跳过分页参数 如果url里边有分页参数 去掉 要保证每次请求一个东西都是第一页
                if (key.equalsIgnoreCase("pageNum")){
                    continue;
                }

                //value是搜索的值
                String value = entry.getValue();
                url+=key+"="+value+"&";

                //排序参数跳过
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")){
                    continue;
                }
                //排序路径
                sortUrl+=key+"="+value+"&";
            }

            //去掉最后一个&
            url = url.substring(0,url.length()-1);
            sortUrl=sortUrl.substring(0,sortUrl.length()-1);
        }
        return new String[]{url,sortUrl};
    }

    /**
     * 替换特殊字符
     * @param searchMap
     */
    private void handleSearchMap(Map<String,String> searchMap){
        if (searchMap!=null){
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if (entry.getKey().startsWith("spec_")){

                    entry.setValue(entry.getValue().replace("+","%2B"));
                }
            }
        }
    }
}
