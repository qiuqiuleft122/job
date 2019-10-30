package com.icicle.search.dao;

import com.icicle.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author Max
 * @Date 19:41 2019/8/31
 * @Description：
 **/
@Repository
public interface SkuESMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
