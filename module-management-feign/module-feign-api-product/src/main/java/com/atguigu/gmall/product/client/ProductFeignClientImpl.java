package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductFeignClientImpl implements ProductFeignClient {
    @Override
    public Result<BaseTrademark> getById(Long tmId) {
        Result<BaseTrademark> result=new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;
    }

    @Override
    public Result<List<JSONObject>> getAllCategoryForPortal() {
        Result<List<JSONObject>> result=new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;

    }

    @Override
    public Result<List<BaseAttrInfo>> getAttrList(Long skuId) {
        Result<List<BaseAttrInfo>> result=new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;
    }

    @Override
    public Result<SkuInfo> getSkuInfo(Long skuId) {
      Result<SkuInfo>  result =new Result<>();
      result=Result.fail();
      result.setMessage("服务降级");
      return result;
    }

    @Override
    public Result<BaseCategoryView> getCategoryViewByCategory3Id(Long category3Id) {
        Result<BaseCategoryView>  result =new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;
    }

    @Override
    public Result<BigDecimal> getSkuPrice(Long skuId) {
        Result<BigDecimal>  result =new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;
    }

    @Override
    public Result<List<SpuSaleAttr>> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        Result<List<SpuSaleAttr>>  result =new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;
    }

    @Override
    public Result<Map> getSkuValueIdsMap(Long spuId) {
        Result<Map>  result =new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;
    }
}
