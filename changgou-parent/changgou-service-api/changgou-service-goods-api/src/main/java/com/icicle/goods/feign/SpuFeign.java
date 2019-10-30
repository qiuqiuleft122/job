package com.icicle.goods.feign;

import com.icicle.goods.pojo.Spu;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Max
 * @Date 19:20 2019/9/8
 * @Description：远程查询Spu信息
 **/
@FeignClient(name = "goods")
@RequestMapping("/spu")
public interface SpuFeign {

    /***
     * 根据SpuID查询Spu信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Spu> findById(@PathVariable(value = "id") Long id);
}
