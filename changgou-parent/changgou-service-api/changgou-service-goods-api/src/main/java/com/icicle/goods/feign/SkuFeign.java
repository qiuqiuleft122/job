package com.icicle.goods.feign;

import com.icicle.goods.pojo.Sku;
import com.icicle.goods.pojo.Spu;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author Max
 * @Date 19:36 2019/8/31
 * @Description：Sku导入ES实现，远程调用
 **/
@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /**
     * 根据状态审核sku
     * @param status 状态
     * @return
     */
    @RequestMapping(value = "/status/{status}")
    Result<List<Sku>> findByStatus(@PathVariable(value = "status")String status);

    /***
     * 根据ID查询SKU信息
     * @param id : sku的ID
     */
    @GetMapping(value = "/{id}")
    Result<Sku> findById(@PathVariable(value = "id", required = true) Long id);

    /**
     * 商品信息递减
     * Map<key,value> key:要递减的商品的ID  value：要递减的商品的数量
     * @param decrMap
     * @return
     */
    @PostMapping(value = "/decr/count")
    Result decrCount(@RequestParam Map<String,Integer> decrMap);

}
