package com.icicle.goods.controller;

import com.github.pagehelper.PageInfo;
import com.icicle.goods.pojo.Brand;
import com.icicle.goods.service.BrandService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Max
 * @Date 17:14 2019/8/25
 * @Description：品牌控制层
 **/
@RestController
@RequestMapping("/brand")
public class BrandController {

    //注入service
    @Autowired
    private BrandService brandService;

    /**
     * 根据分类实现品牌列表查询
     * @param cid 分类ID
     * @return
     */
    @GetMapping(value = "/category/{id}")
    public Result<List<Brand>> findByCategoryId(@PathVariable(value = "id")Integer cid){
        List<Brand> categoryList = brandService.findByCategoryId(cid);
        return new Result<>(true,StatusCode.OK,"查询分类相关的品牌数据成功！",categoryList);
    }

    /**
     * 多条件+分页查询
     * @param brand 查询条件
     * @param pageNum 页码
     * @param size 页大小
     * @return 分页结果
     */
    @PostMapping(value = "/search/{pageNum}/{size}")
    public Result<PageInfo<Brand>> findPage(@RequestBody(required = false) Brand brand,
                                        @PathVariable(value = "pageNum") Integer pageNum,
                                        @PathVariable(value = "size") Integer size){
        //制造异常
//        int num = 1/0;

        PageInfo<Brand> pageInfo = brandService.findPage(brand, pageNum, size);
        return new Result<PageInfo<Brand>>(true, StatusCode.OK, "多条件分页查询品牌成功",pageInfo);
    }

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param size 每页显示数据条数
     * @return
     */
    @GetMapping("/search/{pageNum}/{size}")
    public Result<PageInfo<Brand>> findPage(@PathVariable(value = "pageNum") Integer pageNum,
                                        @PathVariable(value = "size") Integer size){
        PageInfo<Brand> pageInfo = brandService.findPage(pageNum, size);
        return new Result<PageInfo<Brand>>(true, StatusCode.OK, "分页查询品牌成功",pageInfo);
    }

    /**
     * 多条件搜索品牌  这里需要注意
     * @param brand
     * @return
     */
    @PostMapping("/search")
    public Result<List<Brand>> findList(@RequestBody(required = false) Brand brand){
        //多条件查询品牌
        List<Brand> brands = brandService.findList(brand);
        return new Result<>(true, StatusCode.OK, "多条件查询品牌成功",brands);
    }

    /**
     * 根据id删除品牌
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable(value = "id") Integer id){
        brandService.delete(id);
        return new Result<>(true, StatusCode.OK, "删除品牌成功");
    }

    /**
     * 修改品牌  方式一
     * @param brand
     * @return
     */
    @PutMapping
    public Result update(@RequestBody Brand brand) {
        //调用Service实现修改
        brandService.update(brand);
        return new Result<>(true, StatusCode.OK, "修改品牌成功");
    }

//    /**
//     * 修改品牌 方式二   注意：此种方式的请求url方式和查询一个url一致 会产生冲突 不要一起用
//     * @param brand
//     * @param id 根据id修改
//     * @return
//     */
//    @PutMapping(value = "/{id}")
//    public Result update1(@RequestBody Brand brand,@PathVariable(value = "id") Integer id){
//        //设置ID
//        brand.setId(id);
//        //调用service实现修改
//        brandService.update(brand);
//        return new Result<>(true, StatusCode.OK, "修改品牌成功");
//    }

    /**
     * 增加品牌
     *
     * @param brand
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Brand brand) {
        //调用Service新增数据
        brandService.add(brand);
        return new Result<>(true, StatusCode.OK, "新增品牌成功");
    }

    /**
     * 根据id查询所有
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}")
    public Result<Brand> findById(@PathVariable(value = "id") Integer id) {
        //调用Service查询数据
        Brand brand = brandService.findById(id);
        return new Result<>(true, StatusCode.OK, "根据id查询品牌成功", brand);
    }

    /**
     * 查询所有
     *
     * @return
     */
    @GetMapping
    public Result<List<Brand>> findAll() {
        //调用Service查询数据
        List<Brand> brands = brandService.findAll();
        return new Result<>(true, StatusCode.OK, "查询所有操作成功", brands);
    }

}
