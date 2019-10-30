package com.icicle.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author Max
 * @Date 17:09 2019/9/3
 * @Description：
 **/
@FeignClient(name = "search")
@RequestMapping("/search")
public interface SkuFeign {

    /**
     * 调用搜索实现
     * @param searchMap
     * @return
     */
    @GetMapping
    Map<String,Object> search(@RequestParam(required = false)Map<String,String> searchMap);
}
