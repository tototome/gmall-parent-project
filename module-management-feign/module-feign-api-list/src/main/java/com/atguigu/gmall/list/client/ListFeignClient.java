package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-list",fallback = ListFeignClientFallback.class)
public interface ListFeignClient {
    @GetMapping("/api/list/inner/import/sku/to/elastic/search/{skuId}")
     Result<Void> getGoods(@PathVariable("skuId") Long skuId);


    @GetMapping("/api/list/inner/remove/goods/from/elastic/search/{skuId}")
    Result<Void> removeGoodsFromElasticSearch(@PathVariable("skuId") Long skuId) ;

    //热度功能 使用redis 每次访问商品数据的时候的时候调用
    @GetMapping("/api/list/inner/incr/goods/hot/score/{skuId}")
    Result<Void> incrGoodsHotScore(@PathVariable("skuId") Long skuId);

    @PostMapping("/api/list/do/search")
    Result list(@RequestBody SearchParam searchParam) throws Throwable;

}
