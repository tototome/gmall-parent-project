package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import feign.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/user")
public class ApiUserAddress {
    @Autowired
    UserAddressService userAddressService;
    @GetMapping("/auth/getUserAddressList")
    public Result<List<UserAddress>> getUserAddressList(@RequestHeader("userId") Long userId) {
        if (StringUtils.isEmpty(userId)) {
            return Result.<List<UserAddress>>fail().message("没有获取到有效 userId！");
        }
        List<UserAddress> userAddressList =userAddressService.getUserAddressList(userId);
      return Result.ok(userAddressList);
    }
}
