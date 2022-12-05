package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    //  @GetMapping("/inner/incr/goods/hot/score/{skuId}")
    //热度功能 使用redis 每次访问商品数据的时候的时候调用
    @GetMapping("/inner/incr/goods/hot/score/{skuId}")
    public Result<Void> incrGoodsHotScore(@PathVariable("skuId") Long skuId) {
        searchService.incrHotScore(skuId);
        return Result.ok();
    }
    //搜索功能  前端传入搜索条件  返回的是一个VO
    @PostMapping("/do/search")
    //这里直接使用result 在后面接收的时候直接 转map了 Result<Map> 确实狡猾
    public Result list(@RequestBody SearchParam searchParam){
      SearchResponseVo responseVo  =searchService.doSearch(searchParam);
      return Result.ok(responseVo);
    }


}
