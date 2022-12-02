package com.atguigu.gmall.product.controller.api;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product/")
public class ApiProductController {
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    BaseCategoryViewService baseCategoryViewService;
    @Autowired
    SpuSaleAttrService spuSaleAttrService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    BaseAttrInfoService baseAttrInfoService;
    @Autowired
    BaseTrademarkService baseTrademarkService;

    @GetMapping("/inner/getSkuInfo/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId) {

        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        return Result.ok(skuInfo);
    }

    @GetMapping("/inner/getCategoryView/{category3Id}")
    public Result<BaseCategoryView> getCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id) {
        BaseCategoryView byId = baseCategoryViewService.getById(category3Id);
        return Result.ok(byId);
    }

    @GetMapping("/inner/getSkuPrice/{skuId}")
    public Result<BigDecimal> getSkuPrice(@PathVariable("skuId") Long skuId) {
        BigDecimal skuPrice = skuInfoService.getSkuPrice(skuId);
        return Result.ok(skuPrice);
    }

    @GetMapping("/inner/getAttrList/{skuId}")
    public  Result<List<BaseAttrInfo>> getAttrList(@PathVariable("skuId") Long skuId){
        List<BaseAttrInfo> attrList=baseAttrInfoService.getAttrList(skuId);
        return Result.ok(attrList);
    }

    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId,
                                                          @PathVariable("spuId") Long spuId) {
           List<SpuSaleAttr> spuSaleAttrList =spuSaleAttrService.getSpuSaleAttrListCheckBySku(skuId,spuId);
           return Result.ok(spuSaleAttrList);
    }

    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Result<Map> getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
         Map  skuValueIdsMap =skuSaleAttrValueService.getSkuValueIdsMap(spuId);
         return  Result.ok(skuValueIdsMap);
    }

    @GetMapping("inner/get/all/category/for/portal")
    public Result<List<JSONObject>>  getAllCategoryForPortal(){
     List<JSONObject>   categoryForPortal=baseCategoryViewService.getAllCategoryForPortal();
        return Result.ok(categoryForPortal);
    }
    @GetMapping("inner/get/{tmId}")
    public  Result<BaseTrademark> getById(@PathVariable("tmId") Long tmId){
        BaseTrademark baseTrademark = baseTrademarkService.getById(tmId);
        return  Result.ok(baseTrademark);
    }

}
