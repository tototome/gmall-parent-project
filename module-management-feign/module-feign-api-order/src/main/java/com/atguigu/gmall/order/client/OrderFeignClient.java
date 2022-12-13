package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "service-order",path = "api/order")
public interface OrderFeignClient {
    @GetMapping("/inner/auth/trade")
    Result<Map<String, Object>> trad();
}
