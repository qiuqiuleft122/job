package com.icicle.search.controller;

import com.icicle.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author Max
 * @Date 20:21 2019/8/31
 * @Description：
 **/
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 导入sku数据到es
     * @return
     */
    @RequestMapping(value = "/import")
    public Result importSku(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库成功");
    }

    /**
     * 调用搜索实现
     * @param searchMap 前端传入的搜索条件  required = false 有可能搜索条件为空
     * @return
     */
    @GetMapping
    public Map<String,Object> search(@RequestParam(required = false)Map<String,String> searchMap){
        return skuService.search(searchMap);
    }
}
