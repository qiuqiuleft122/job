package com.icicle.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.icicle.goods.dao.BrandMapper;
import com.icicle.goods.dao.CategoryMapper;
import com.icicle.goods.dao.SkuMapper;
import com.icicle.goods.dao.SpuMapper;
import com.icicle.goods.pojo.*;
import com.icicle.goods.service.SpuService;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:Max
 * @Description:Spu业务层接口实现类
 * @Date 2019/1/28
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired(required = false)
    private SpuMapper spuMapper;

    //注入ID生成器
    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private SkuMapper skuMapper;

    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Autowired(required = false)
    private BrandMapper brandMapper;

    /**
     * 还原被删除商品  恢复数据
     * @param spuId
     */
    @Override
    public void restore(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否删除
        if (!spu.getIsDelete().equalsIgnoreCase("1")) {
            //表示未删除
            throw new RuntimeException("此商品未删除！");
        }
        //确定是删除的
        //设置未删除
        spu.setIsDelete("0");

        //设置审核状态 只是恢复数据 并未审核
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 逻辑删除
     * @param spuId 需要删除的商品id
     */
    @Override
    public void logicDelete(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否下架的商品  下架的商品才能删除
        if (!spu.getIsMarketable().equalsIgnoreCase("0")) {
            throw new RuntimeException("必须先下架再删除！");
        }
        //删除
        spu.setIsDelete("1");
        //逻辑删除之后 数据库 还是存在的 需要改掉审核状态
        spu.setStatus("0");
        //执行删除  说白了就是更新
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 批量下架
     * @param spuIds 需要下架的所有商品ID
     * @return 返回下架的商品数量
     */
    @Override
    public int pullMany(Long[] spuIds) {
        //准备修改的数据
        Spu spu = new Spu();
        spu.setIsMarketable("0");
        //update tb_spu set IsMarketable=1 where id in(ids) and isDelete=0 and isMarketable=1 批量上架的分析  需要组装的条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //id in(ids)
        criteria.andIn("id",Arrays.asList(spuIds));
        //未删除
        criteria.andEqualTo("isDelete","0");
        //审核通过的 [上架之后才存在下架  上架肯定是通过审核的 这个不需要在判断]
        //criteria.andEqualTo("Status","1");
        //上架的商品才存在下架的价值
        criteria.andEqualTo("isMarketable","1");

        //下架操作
        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 批量上架  说白了 多条件 情况下 修改状态
     * @param spuIds 前端传递一组商品ID 要上架的所有商品ID
     * @return 返回批量上架的商品数量
     */
    @Override
    public int putMany(Long[] spuIds) {

        //准备修改的数据
        Spu spu = new Spu();
        spu.setIsMarketable("1");

        //update tb_sku set IsMarketable=1 where id in(ids) and isdelete=0 and status=1 批量上架的分析  需要组装条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //id in(ids)  注意AndIn这个组装方法  里边的第二个参数是集合类型
        criteria.andIn("id", Arrays.asList(spuIds));
        //未删除的
        criteria.andEqualTo("isDelete","0");
        //下架的
        criteria.andEqualTo("isMarketable","0");
        //审核通过的
        criteria.andEqualTo("Status","1");

        //上架操作
        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 商品上架
     * @param spuId 公共属性的spuId
     */
    @Override
    public void put(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        //判断是否符合上架条件
        if (spu.getIsDelete().equalsIgnoreCase("1")) {
            throw new RuntimeException("此商品已删除！");
        }
        //审核状态 审核通过的商品才能上架  这里不能直接
        if (!spu.getStatus().equalsIgnoreCase("1")){
            throw new RuntimeException("未通过审核的商品不能上架！");
        }
        //上架状态
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 商品下架
     * @param spuId 公共属性的spuId
     */
    @Override
    public void pull(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        //判断商品是否符合下架条件
        if(spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("不能对已删除的商品进行下架！");
        }

        if(spu.getIsMarketable().equalsIgnoreCase("0")){
            throw new RuntimeException("不能对已已下架的商品进行下架！");
        }

        //下架状态
        spu.setIsMarketable("0"); //下架
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 商品审核
     * @param spuId 公共属性的spuId
     */
    @Override
    public void audit(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断商品是否符合审核条件
        if (spu.getIsDelete().equalsIgnoreCase("1")) {
            throw new RuntimeException("不能对已删除的商品进行审核！");
        }
        //审核状态
        spu.setStatus("1"); //审核通过
        spu.setIsMarketable("1"); //审核通过自动上架
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 根据SPU的ID查找SPU以及对应的SKU集合
     * @param spuId 公共属性spuId
     * @return
     */
    @Override
    public Goods findGoodsById(Long spuId) {
        //查询Spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //查询List<Sku>->spuId  select * from tb_sku where spu_id=?
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        //封装到goods中
        return new Goods(spu,skuList);
    }

    /**
     * 新增商品  【包含修改商品】
     * @param goods 新增的商品信息
     */
    @Override
    public void saveGoods(Goods goods) {
        //增加Spu->1个
        Spu spu = goods.getSpu();

        //判断Spu的ID是否为空
        if (spu.getId() != null) {
            //不为空，则修改  先修改spu
            spuMapper.updateByPrimaryKeySelective(spu);
            //修改sku  先删除 再新增【逻辑问题】
            //删除之前的List<Sku>  delete from tb_sku where spu_id=?
            //封装一个javabean，如果该javabean指定的属性不为空，则会将指定属性作为查询条件
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            //先执行删除操作
            skuMapper.delete(sku);
        }else {
            //否则，就是新增  Spu表的id不是自增的 需要自己设置 id生成器
            spu.setId(idWorker.nextId());
            spuMapper.insertSelective(spu);
        }

        //操作新增Sku ->list (多个)  但是数据库里边的有些数据 还是需要自己手动设置  前端无法传

        //三级分类
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        //品牌名称
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());

        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {

            sku.setId(idWorker.nextId());

            //设置sku表的name  就是你这个商品的名字  是由spu的name和sku里边的spec规格组成
            StringBuilder name = new StringBuilder(spu.getName());

            //获取spec的值  {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}
            //防止空指针异常
            if (StringUtils.isEmpty(sku.getSpec())) {
                sku.setSpec("{}");
            }

            //spec的值本身是一个string类型的 但是数据格式 和map一模一样  转化成map 将value取出 拼接即可
            //将spec规格转换成Map  使用fastJson
            Map<String,String> specMap = JSON.parseObject(sku.getSpec(),Map.class);
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                name.append(" ").append(entry.getValue());
            }
            sku.setName(name.toString()); //Spu.name+规格信息 页面商品的标题
            sku.setCreateTime(new Date());
            sku.setUpdateTime(new Date());
            sku.setSpuId(spu.getId());
            sku.setCategoryId(spu.getCategory3Id());   //分类ID->3级分类ID
            sku.setCategoryName(category.getName());  //分类名字->3级分类名字
            sku.setBrandName(brand.getName());  //品牌名称

            //将sku添加到数据库中
            skuMapper.insertSelective(sku);
        }

    }

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 物理删除 【逻辑存在】
     * @param spuId
     */
    @Override
    public void delete(Long spuId){
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否被逻辑删除  ,必须先逻辑删除后才能物理删除 说白了就是删除状态是已删除
        if (!spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("此商品不能被删除！");
        }
        //其他状态执行删除
        spuMapper.deleteByPrimaryKey(spuId);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}
