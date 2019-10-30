package com.icicle.goods.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Max
 * @Date 20:34 2019/8/28
 * @Description：组合实体类 用于接收spu和sku  保存时使用
 * 商品信息组合对象
 *  List<Sku>
 *  Spu
 **/
public class Goods implements Serializable {

    //Spu信息
    private Spu spu;

    //Sku集合信息
    private List<Sku> skuList;

    public Goods() {
    }

    public Goods(Spu spu, List<Sku> skuList) {
        this.spu = spu;
        this.skuList = skuList;
    }

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
