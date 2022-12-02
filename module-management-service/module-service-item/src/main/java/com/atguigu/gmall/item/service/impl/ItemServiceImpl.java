package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.util.ThreadPoolUtil;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    private ListFeignClient listFeignClient;
    @Override
    public Result<Map<String, Object>> getItem(Long skuId) throws ExecutionException, InterruptedException {
        //SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId).getData();
        //Long spuId = skuInfo.getSpuId();
        //Long category3Id = skuInfo.getCategory3Id();
        //BaseCategoryView categoryViewByCategory3Id = productFeignClient.getCategoryViewByCategory3Id(category3Id).getData();
        //BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId).getData();
        //Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(spuId).getData();
        //String valueIdsJson = JSON.toJSONString(skuValueIdsMap);
        //List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, spuId).getData();
        ////进行优化 使用多线程的方式 优化
        Map<String, Object> result = new HashMap<>();
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor();
        //Supplier<U> supplier 获得一个返回值 不需要传入值
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync( ()-> {
            return   productFeignClient.getSkuInfo(skuId).getData();
        }, threadPoolExecutor);
        //异步
        CompletableFuture<BaseCategoryView> baseCategoryViewCompletableFuture = skuInfoCompletableFuture.thenApplyAsync(((SkuInfo skuInfo) -> {
            Long spuId = skuInfo.getSpuId();
            Long category3Id = skuInfo.getCategory3Id();
            return productFeignClient.getCategoryViewByCategory3Id(category3Id).getData();
        }), threadPoolExecutor);


        CompletableFuture<BigDecimal> bigDecimalCompletableFuture = CompletableFuture.supplyAsync( ()->productFeignClient.getSkuPrice(skuId).getData()
        , threadPoolExecutor);
        CompletableFuture<String> stringCompletableFuture = skuInfoCompletableFuture.thenApplyAsync((SkuInfo skuInfo) -> {
            Long spuId = skuInfo.getSpuId();
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(spuId).getData();
            return JSON.toJSONString(skuValueIdsMap);
        }, threadPoolExecutor);
        CompletableFuture<List<SpuSaleAttr>> listCompletableFuture = skuInfoCompletableFuture.thenApplyAsync((SkuInfo skuInfo) -> {
            Long spuId = skuInfo.getSpuId();
            return productFeignClient.getSpuSaleAttrListCheckBySku(skuId, spuId).getData();
        }, threadPoolExecutor);
        // -------------------附加任务：增加商品热度值-------------------
        CompletableFuture.runAsync(()->{
            listFeignClient.incrGoodsHotScore(skuId);
        }, threadPoolExecutor);

        SkuInfo skuInfo = skuInfoCompletableFuture.get();
        BaseCategoryView categoryViewByCategory3Id = baseCategoryViewCompletableFuture.get();
        BigDecimal skuPrice = bigDecimalCompletableFuture.get();
        String valueIdsJson = stringCompletableFuture.get();
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = listCompletableFuture.get();

        // 保存 skuInfo
        result.put("skuInfo",skuInfo);

        // 保存 spuSaleAttrList
        result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);

        // 保存 valuesSkuJson
        result.put("valuesSkuJson",valueIdsJson);

        // 保存价格
        result.put("price",skuPrice);

        // 保存商品分类数据
        result.put("categoryView",categoryViewByCategory3Id);

        return Result.ok(result);
    }
}
