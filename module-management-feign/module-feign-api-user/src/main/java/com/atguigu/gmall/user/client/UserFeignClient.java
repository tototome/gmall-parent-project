package com.atguigu.gmall.user.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(value = "service-user")
public interface UserFeignClient {

    @GetMapping("api/user/auth/getUserAddressList")
    Result<List<UserAddress>> getUserAddressList(@RequestHeader("userId") Long userId);
}
