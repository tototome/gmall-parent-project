package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/list")
public class ListController {
    @Autowired
    private SearchService searchService;

    @GetMapping("/inner/import/sku/to/elastic/search/{skuId}")
    public Result<Void> getGoods(@PathVariable("skuId") Long skuId){
            searchService.importGoodsToElasticSearch(skuId);
            return Result.ok();
    }

    @GetMapping("inner/remove/goods/from/elastic/search/{skuId}")
    public Result<Void> removeGoodsFromElasticSearch(@PathVariable("skuId") Long skuId) {
        searchService.removeGoodsFromElasticSearch(skuId);
        return Result.ok();
    }
    //热度功能 使用redis 每次访问商品数据的时候的时候调用
    @GetMapping("/inner/incr/goods/hot/score/{skuId}")
    public Result<Void> incrGoodsHotScore(@PathVariable("skuId") Long skuId) {

        searchService.incrHotScore(skuId);

        return Result.ok();
    }
}
