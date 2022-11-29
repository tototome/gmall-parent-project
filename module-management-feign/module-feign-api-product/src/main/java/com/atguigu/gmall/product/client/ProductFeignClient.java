package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//指定客户端的名称
@FeignClient(value = "service-product",fallback = ProductFeignClientImpl.class)
public interface ProductFeignClient {

    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    Result<BaseCategoryView> getCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id);

    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    Result<BigDecimal> getSkuPrice(@PathVariable("skuId") Long skuId);

    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    Result<List<SpuSaleAttr>> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId,
                                                   @PathVariable("spuId") Long spuId);
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    Result< Map >getSkuValueIdsMap(@PathVariable("spuId") Long spuId);
    @GetMapping("/api/product/inner/get/all/category/for/portal")
     Result<List<JSONObject>>  getAllCategoryForPortal();
}
