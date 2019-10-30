package com.icicle.content.feign;
import com.icicle.content.pojo.Content;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:Max
 * @Description:feign远程调用 更新redis中广告缓存
 * @Date 2019/1/28
 *****/
@FeignClient(name="content")
@RequestMapping("/content")
public interface ContentFeign {

    /**
     * 根据id查询广告
     * @param categoryId
     * @return
     */
    @GetMapping(value = "/list/category/{id}")
    Result<List<Content>> findByCategoryId(@PathVariable(value = "id")Long categoryId);
}