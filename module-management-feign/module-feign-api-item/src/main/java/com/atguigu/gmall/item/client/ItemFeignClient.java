package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(value = "service-item",fallback = ItemFeignClientFallback.class)
public interface ItemFeignClient {

    @GetMapping("api/item/{skuId}")
    Result<Map<String, Object>> getItem(@PathVariable("skuId") Long skuId);


}
