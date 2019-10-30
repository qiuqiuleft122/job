package com.icicle.goods.controller;

import com.github.pagehelper.PageInfo;
import com.icicle.goods.pojo.Album;
import com.icicle.goods.service.AlbumService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Max
 * @Date 19:55 2019/8/27
 * @Description：相册控制层
 **/
@RestController
@RequestMapping("/album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    /**
     * 查询所有
     * @return 返回result
     */
    @GetMapping
    public Result<List<Album>> findAll(){
        List<Album> albums = albumService.findAll();
        return new Result<>(true, StatusCode.OK,"查询所有相册成功",albums);
    }

    /**
     * 根据id查询相册
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}")
    public Result<Album> findById(@PathVariable(value = "id")Integer id){
        Album album = albumService.findById(id);
        return new Result<>(true, StatusCode.OK,"根据id查询相册成功",album);
    }

    /**
     * 新增album
     * @param album 前端传过来的album对象
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Album album){
        albumService.add(album);
        return new Result<>(true, StatusCode.OK,"新增相册成功",album);
    }

    /**
     * 修改album
     * @param album  前端传过来的album对象
     * @param id album的id主键
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Album album,@PathVariable(value = "id") Long id){
        //设置主键值
        album.setId(id);
        //调用service修改
        albumService.update(album);
        return new Result<>(true, StatusCode.OK,"修改相册成功",album);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Long id){
        albumService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * Album分页搜索实现
     * @param pageNum:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{pageNum}/{size}" )
    public Result<PageInfo> findPage(@PathVariable(value = "pageNum")  Integer pageNum,
                                     @PathVariable(value = "size")  Integer size){
        //分页查询
        PageInfo<Album> pageInfo = albumService.findPage(pageNum, size);
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * Album多条件分页搜索实现
     * @param album 查询条件
     * @param pageNum:当前页
     * @param size:每页显示多少条
     * @return
     */
    @PostMapping(value = "/search/{pageNum}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false) Album album,
                                     @PathVariable(value = "pageNum")  Integer pageNum,
                                     @PathVariable(value = "size")  Integer size){
        //分页查询
        PageInfo<Album> pageInfo = albumService.findPage(album,pageNum, size);
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
    }


}
