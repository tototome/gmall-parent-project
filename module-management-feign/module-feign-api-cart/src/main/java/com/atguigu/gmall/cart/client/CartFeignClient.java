package com.atguigu.gmall.cart.client;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


@FeignClient(value = "service-cart")
public interface CartFeignClient {

    @GetMapping ("/api/cart/addCart/{skuId}/{skuNum}")
    Result<Void> addCart(@PathVariable("skuId") Long skuId,
                                @PathVariable("skuNum") Integer skuNum);

    @GetMapping("/api/cart/auth/get/checked/cart")
   Result<List<CartInfo>> getCheckedCart(@RequestHeader("userId") Long userId);

}