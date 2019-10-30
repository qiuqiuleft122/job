package com.icicle.search.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @Author Max
 * @Date 19:13 2019/8/31
 * @Description：文档映射Bean创建
 *
 * JavaBean数据存入到ES中要以搜索条件和搜索展示结果为依据
 **/
@Document(indexName = "skuinfo",type = "docs")
public class SkuInfo implements Serializable {
    //商品id，同时也是商品编号
    @Id  //注意：记住这里springdata集成es  注解使用这个import org.springframework.data.annotation.Id;   不是springdata spa  哪里的javax里边的
    private Long id;

    /***
     * SKU名称
     * type = FieldType.Text:类型，Text支持分词
     * index = true:添加数据的时候，是否分词  默认的可以不用配
     * analyzer = "ik_smart":创建索引的分词器
     * store = false:是否存储 默认的可以不用配
     * searchAnalyzer = "ik_smart":搜索时候使用的分词器
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String name;

    //商品价格，单位为：元
    @Field(type = FieldType.Double)
    private Long price;

    //库存数量  【也可以不用加上@Field注解 索引库 会默认创建域的】
    private Integer num;

    //商品图片
    private String image;

    //商品状态，1-正常，2-下架，3-删除
    private String status;

    //创建时间
    private Date createTime;

    //更新时间
    private Date updateTime;

    //是否默认
    private String isDefault;

    //SPUID
    private Long spuId;

    //类目ID
    private Long categoryId;

    /****
     * 类目名称
     * type = FieldType.Keyword:不分词  分类的名字不用分词的 比如 华为手机  如果分词 华为和手机 搜出来的结果肯定不一样
     */
    @Field(type = FieldType.Keyword)
    private String categoryName;

    /***
     * 品牌名称
     * type = FieldType.Keyword:不分词
     */
    @Field(type = FieldType.Keyword)
    private String brandName;

    //规格
    private String spec;  //Map(String)->Map类型

    //规格参数
    private Map<String,Object> specMap;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public Long getSpuId() {
        return spuId;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Map<String, Object> getSpecMap() {
        return specMap;
    }

    public void setSpecMap(Map<String, Object> specMap) {
        this.specMap = specMap;
    }
}
