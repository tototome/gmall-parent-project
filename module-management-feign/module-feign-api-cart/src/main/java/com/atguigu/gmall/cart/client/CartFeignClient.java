package com.atguigu.gmall.cart.client;


import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "service-cart")
public interface CartFeignClient {

    @GetMapping ("/api/cart/addCart/{skuId}/{skuNum}")
    Result<Void> addCart(@PathVariable("skuId") Long skuId,
                                @PathVariable("skuNum") Integer skuNum);

}