package com.icicle.search.service;

import java.util.Map;

/**
 * @Author Max
 * @Date 19:44 2019/8/31
 * @Description：搜索服务层接口
 **/
public interface SkuService {

    /**
     * 搜索
     * @param searchMap 前端传入的搜索条件 包括关键字 规格
     * @return map
     */
    Map<String,Object> search(Map<String,String> searchMap);

    /**
     * 导入sku数据
     */
    void importSku();
}
