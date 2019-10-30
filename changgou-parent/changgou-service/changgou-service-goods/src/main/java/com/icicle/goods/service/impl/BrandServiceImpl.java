package com.icicle.goods.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.icicle.goods.dao.BrandMapper;
import com.icicle.goods.pojo.Brand;
import com.icicle.goods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author Max
 * @Date 17:03 2019/8/25
 * @Description：品牌服务的接口实现类
 **/
@Service
public class BrandServiceImpl implements BrandService {

    //注入dao
    @Autowired(required = false)
    private BrandMapper brandMapper;


    /**
     * 查询分类对应的品牌集合
     * @param cid 分类id 商品添加时需要
     * @return
     */
    @Override
    public List<Brand> findByCategoryId(Integer cid) {
        return brandMapper.findByCategoryId(cid);
    }

    /**
     * 查询所有
     *
     * @return
     */
    @Override
    public List<Brand> findAll() {
        //使用通用Mapper查询所有
        return brandMapper.selectAll();
    }

    /**
     * 根据id查询品牌
     *
     * @param id 品牌id
     * @return
     */
    @Override
    public Brand findById(Integer id) {
        //通用Mapper：selectByPrimaryKey:根据ID查询
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增品牌
     *
     * @param brand 品牌对象
     */
    @Override
    public void add(Brand brand) {
        /***
         * insertSelective：增加操作,忽略空值
         * brand.id=null
         * brand.name=华为6666
         * brand.image=null
         * brand.letter=H
         * brand.seq=null
         *
         * 只要方法中带有Selective都会忽略空值
         * INSERT INTO tb_brand(name,letter) VALUES(?,?)
         *
         * brandMapper.insert(brand);
         * INSERT INTO tb_brand(id,name,image,letter,seq) VALUES(?,?,?,?,?)
         */
        brandMapper.insertSelective(brand);
    }

    /**
     * 修改品牌
     *
     * @param brand 品牌对象
     */
    @Override
    public void update(Brand brand) {
        //通用Mapper修改数据，忽略空值 根据主键id 查询
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    @Override
    public void delete(Integer id) {
        //根据主键id删除
        brandMapper.deleteByPrimaryKey(id);
    }

    /**
     * 多条件查询 比如 id  name  letter
     * 根据用户输入的条件查询
     * 1)输入name-根据name查询[模糊查询]
     * 2)输入了letter-根据letter查询
     *
     * 这里需要注意 不输入任何条件 查询出来就是所有
     *
     * @param brand
     */
    @Override
    public List<Brand> findList(Brand brand) {
        //进行条件组装
        Example example = createExample(brand);

        return brandMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param pageNum 当前页码
     * @param size 每页显示数据条数
     * @return
     */
    @Override
    public PageInfo<Brand> findPage(Integer pageNum, Integer size) {
        //静态分页 PageHelper.startPage(pageNum,size)
        PageHelper.startPage(pageNum,size);
        //查询
        List<Brand> brands = brandMapper.selectAll();
        //封装PageInfo<T>
        return new PageInfo<Brand>(brands);
    }

    /**
     * 分页加上条件搜索
     * @param brand 封装的搜索条件
     * @param pageNum 当前页码
     * @param size 每页显示数据条数
     * @return
     */
    @Override
    public PageInfo<Brand> findPage(Brand brand, Integer pageNum, Integer size) {
        //静态分页
        PageHelper.startPage(pageNum,size);
        //动态装载
        Example example = createExample(brand);
        //搜索
        List<Brand> brands = brandMapper.selectByExample(example);
        //封装PageInfo<T>
        return new PageInfo<Brand>(brands);
    }

    /**
     * 抽取公共方法  动态条件查询
     * @param brand
     * @return
     */
    private Example createExample(Brand brand) {
        //动态构建条件Example,criteria:动态组装条件  tk包下面的
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        //组装条件 组装之前先判定下 有可能用户没有数据任何条件
        if (brand != null) {
            //id
            if (!StringUtils.isEmpty(brand.getId())) {
                //这个是直接相等  第一个参数 对应实体类上的属性  第二个参数 封装在实体类中 前端用户输入的搜索条件
                criteria.andEqualTo("id", brand.getId());
            }
            //name  模糊查询 select * from tb_brand wehere name like '%brand.getName%'
            if (!StringUtils.isEmpty(brand.getName())) {
                criteria.andLike("name", "%" + brand.getName() + "%");
            }
            //letter
            if (!StringUtils.isEmpty(brand.getLetter())) {
                criteria.andEqualTo("letter",brand.getLetter());
            }
        }
        return example;
    }
}
