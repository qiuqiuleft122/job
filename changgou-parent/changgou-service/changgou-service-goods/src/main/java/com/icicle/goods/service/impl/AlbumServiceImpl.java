package com.icicle.goods.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.icicle.goods.dao.AlbumMapper;
import com.icicle.goods.pojo.Album;
import com.icicle.goods.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author Max
 * @Date 19:49 2019/8/27
 * @Description：相册业务接口实现类
 **/
@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired(required = false)
    private AlbumMapper albumMapper;

    /**
     * 查询所有
     * @return
     */
    @Override
    public List<Album> findAll() {
        return albumMapper.selectAll();
    }

    /**
     * 根据id查询
     * @param id 相册id
     * @return
     */
    @Override
    public Album findById(Integer id) {
        return albumMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增album
     * @param album 新增的相册对象
     */
    @Override
    public void add(Album album) {
        //忽略空值
        albumMapper.insertSelective(album);
    }

    /**
     * 修改album
     * @param album album对象
     */
    @Override
    public void update(Album album) {
        //通用Mapper修改数据，忽略空值 根据主键id 查询
        albumMapper.updateByPrimaryKeySelective(album);
    }

    /**
     * 根据ID删除品牌数据
     * @param id
     */
    @Override
    public void delete(Long id) {
        albumMapper.deleteByPrimaryKey(id);
    }

    /**
     * 多条件查询album
     * @param album
     * @return
     */
    @Override
    public List<Album> findList(Album album) {
        //调用公共方法
        Example example = createExample(album);
        return albumMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param pageNum 当前页码
     * @param size 每页显示数据条数
     * @return
     */
    @Override
    public PageInfo<Album> findPage(Integer pageNum, Integer size) {
        //使用分页插件
        PageHelper.startPage(pageNum,size);
        //查询  此时分页插件 会动态的装载分页条件
        List<Album> albums = albumMapper.selectAll();
        return new PageInfo<>(albums);
    }

    /**
     * Album多条件分页搜索实现
     * @param album 查询条件
     * @param pageNum:当前页
     * @param size:每页显示多少条
     * @return
     */
    @Override
    public PageInfo<Album> findPage(Album album, Integer pageNum, Integer size) {
        //分页插件
        PageHelper.startPage(pageNum,size);
        //动态装载条件
        Example example = createExample(album);
        //查询
        List<Album> albums = albumMapper.selectByExample(example);
        return new PageInfo<>(albums);
    }

    /**
     * 抽取公共方法 动态查询
     * @param album
     * @return
     */
    public Example createExample(Album album){
        //动态装载条件
        Example example = new Example(Album.class);
        Example.Criteria criteria = example.createCriteria();
        //组装条件 组装之前先判定下 有可能用户没有数据任何条件
        if(album!=null){
            // 编号
            if(!StringUtils.isEmpty(album.getId())){
                criteria.andEqualTo("id",album.getId());
            }
            // 相册名称
            if(!StringUtils.isEmpty(album.getTitle())){
                criteria.andLike("title","%"+album.getTitle()+"%");
            }
            // 相册封面
            if(!StringUtils.isEmpty(album.getImage())){
                criteria.andEqualTo("image",album.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(album.getImageItems())){
                criteria.andEqualTo("imageItems",album.getImageItems());
            }
        }
        return example;
    }
}
