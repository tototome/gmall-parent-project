package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

@Component
public class ListFeignClientFallback implements ListFeignClient {
    @Override
    public Result<Void> getGoods(Long skuId) {
        return Result.<Void>fail().message("服务降级");
    }

    @Override
    public Result<Void> removeGoodsFromElasticSearch(Long skuId) {
        return Result.<Void>fail().message("服务降级");
    }

    @Override
    public Result<Void> incrGoodsHotScore(Long skuId) {
        return Result.<Void>fail().message("服务降级");
    }
}
