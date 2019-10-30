package com.icicle.order.controller;

import com.icicle.order.pojo.OrderItem;
import com.icicle.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author Max
 * @Date 16:08 2019/9/9
 * @Description：购物车操作
 **/
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 查询购物车列表
     * @return
     */
    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list(){
        //String username = "jay";
        //之前写死的 名字  现在从用户登录的信息中去取出来
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo); //这个里边很多信息
        String username = userInfo.get("username");
        List<OrderItem> orderItems = cartService.list(username);
        return new Result<>(true,StatusCode.OK,"查询购物车成功",orderItems);
    }

    /**
     * 加入购物车
     * @param num  加入购物车的数量
     * @param id 商品ID
     * @return
     */
    @GetMapping(value = "/add/{num}/{id}")
    public Result add(@PathVariable(value = "num") Integer num,@PathVariable(value = "id") Long id){
        //之前写死的 名字  现在从用户登录的信息中去取出来
        String username = TokenDecode.getUserInfo().get("username");
        cartService.add(num,id,username);
        return new Result(true, StatusCode.OK,"加入购物车成功！");
    }
}
