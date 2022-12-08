package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class ApiPassportController {

    @Autowired
    UserInfoService userInfoService;
    @Autowired
    RedisTemplate redisTemplate;

    //用户登陆是直接发送异步请求 详情参照 login.html和login.js
    //返回值是token和userInfo setUserInfo(JSON.stringify(response.data.data))
    //所以Map类型是<String, String>
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody UserInfo userInfoFromPage, HttpServletRequest request) {
        //根据传入用户的信息判断 数据库是否有这个用户
        UserInfo userInfoLogin = userInfoService.login(userInfoFromPage);
        //判断用户是否为空 空说明用户不存在
        if (userInfoLogin == null) {
            return Result.<Map<String, String>>fail().message("用户名或密码不正确");
        }
        //用户存在 设置token
        String token = UUID.randomUUID().toString().replace("-", "");

        String tokenKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;

        //设置tokenValue userId+IP 拼接IP是为了防止token被到盗用
        //tokenValue 可以放一个JSON过去
        String tokenValue = userInfoLogin.getId() + ":" + IpUtil.getIpAddress(request);
        //存入 redis 设置超时时间
        redisTemplate.opsForValue().set(tokenKey, tokenValue, RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
        //将用户nick 名字和 token返回给前端
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("nickName", userInfoLogin.getNickName());
        return Result.ok(map);
    }

    @GetMapping("logout")
    public Result<Void> logout(HttpServletRequest request) {
        //删除redis中删除token
        // 先获取到token值
        String token = request.getHeader("token");
        String tokenKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        redisTemplate.delete(tokenKey);
        return Result.ok();
    }
}
