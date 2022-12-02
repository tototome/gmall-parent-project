package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-list",fallback = ListFeignClientFallback.class)
public interface ListFeignClient {
    @GetMapping("/inner/import/sku/to/elastic/search/{skuId}")
     Result<Void> getGoods(@PathVariable("skuId") Long skuId);


    @GetMapping("inner/remove/goods/from/elastic/search/{skuId}")
    Result<Void> removeGoodsFromElasticSearch(@PathVariable("skuId") Long skuId) ;

    //热度功能 使用redis 每次访问商品数据的时候的时候调用
    @GetMapping("/inner/incr/goods/hot/score/{skuId}")
    Result<Void> incrGoodsHotScore(@PathVariable("skuId") Long skuId);

}
