package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ItemFeignClientFallback implements ItemFeignClient {
    @Override
    public Result<Map<String, Object>> getItem(Long skuId) {
        Result<Map<String, Object>>  result =new Result<>();
        result=Result.fail();
        result.setMessage("服务降级");
        return result;
    }
}
