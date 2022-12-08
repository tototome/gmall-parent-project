package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public UserInfo login(UserInfo userInfoFromPage) {
        String passwd = userInfoFromPage.getPasswd();
        String encrypt = MD5.encrypt(passwd);
        //根据账号密码查询数据库 返回一个UserInfo
        UserInfo userInfoLogin = userInfoMapper.selectOne(new QueryWrapper<UserInfo>()
                .eq("login_name", userInfoFromPage.getLoginName())
                .eq("passwd", encrypt));
        return userInfoLogin;
    }
}
