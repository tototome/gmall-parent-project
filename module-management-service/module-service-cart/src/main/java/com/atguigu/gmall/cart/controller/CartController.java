package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/addCart/{skuId}/{skuNum}")
    public Result<Void> addCart(@PathVariable("skuId") Long skuId,
                                @PathVariable("skuNum") Integer skuNum,
                                HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        //有正式ID 使用正式Id 没有再使用临时Id  不会存在既没有正式ID也没有临时ID
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }

        cartService.addCart(skuId, skuNum, userId);
        return Result.<Void>ok();
    }

    @GetMapping("/cartList")
    public Result<List<CartInfo>> selectCart(HttpServletRequest request) {
        //购物车是有临时的和正式的 这就涉及到购物车的合并
        //什么时候合并 登陆后 两种情况临时有数据 正式没有数据 临时有数据 正式有数据
        Enumeration<String> userId1 = request.getHeaders("userId");
        String userTempId = AuthContextHolder.getUserTempId(request);
        String userId = AuthContextHolder.getUserId(request);

        List<CartInfo> cartInfoList = cartService.selectCart(userId, userTempId);

        return Result.ok(cartInfoList);


    }

    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result<Void> modifyCartCheckStatus(@PathVariable("skuId") Long skuId,
                                              @PathVariable("isChecked") Integer isChecked,
                                              HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        if (!StringUtils.isEmpty(userId)) {
            cartService.modifyCartCheckStatus(userId, skuId, isChecked);
        }


        if (!StringUtils.isEmpty(userTempId)) {
            cartService.modifyCartCheckStatus(userTempId, skuId, isChecked);
        }

        return Result.ok();
    }
    @DeleteMapping("/deleteCart/{skuId}")
    public Result<Void> deleteCart(@PathVariable("skuId") Long skuId, HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);

        if (!StringUtils.isEmpty(userId)) {
            cartService.removeCartItem(userId, skuId);
        }

        String userTempId = AuthContextHolder.getUserTempId(request);

        if (!StringUtils.isEmpty(userTempId)) {
            cartService.removeCartItem(userTempId, skuId);
        }

        return Result.ok();
    }
}
