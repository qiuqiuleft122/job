package com.icicle.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.icicle.content.feign.ContentFeign;
import com.icicle.content.pojo.Content;
import com.xpand.starter.canal.annotation.*;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Author Max
 * @Date 19:33 2019/8/30
 * @Description：实现对数据库的监听
 **/
@CanalEventListener
public class CanalDataEventListener {

    @Autowired
    private ContentFeign contentFeign;

    //操作redis的模板  封装了jedis操作
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 自定义数据监听  修改广告数据 修改监听
     * 同步到redis
     * @param eventType 当前操作的类型
     * @param rowData 发生变更的一行数据
     */
    @ListenPoint(
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE, CanalEntry.EventType.INSERT},  //监听操作的类型
            schema = {"changgou_content"},  //指定需要监控的数据库
            table = {"tb_content"},   //指定需要监控的表
            destination = "example"   //指定实例的地址  就是instance.properties的文件所在位置  可以不用配
    )
    public void onEventCustom(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        //获取广告分类的ID
        String categoryId = getColumn(rowData, "category_id");
        //根据广告分类ID获取所有广告
        Result<List<Content>> result = contentFeign.findByCategoryId(Long.valueOf(categoryId));
        //将广告数据存入到Redis缓存
        //首先取出result中的数据  拼接分类id作为key
        List<Content> contentList = result.getData();
        stringRedisTemplate.boundValueOps("content_"+categoryId).set(JSON.toJSONString(contentList));
    }


    /**
     * 获取指定列的值
     * @param rowData 变更的
     * @param columnName  传入的需要匹配的列名
     * @return
     */
    public String getColumn(CanalEntry.RowData rowData,String columnName){
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            //获取肯定修改之后的  列名 根据这个列名 查找修改后的数据  需要传入列名进行匹配
            if (column.getName().equals(columnName)) {
                return column.getValue();
            }
        }
        //当然有可能是删除操作 那就要获取删除前
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            //获取肯定修改之后的  列名 根据这个列名 查找修改后的数据  需要传入列名进行匹配
            if (column.getName().equals(columnName)) {
                return column.getValue();
            }
        }

        return null;
    }



}
