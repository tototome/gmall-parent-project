package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    UserAddressMapper userAddressMapper;
    @Override
    public List<UserAddress> getUserAddressList(Long userId) {
        List<UserAddress> userAddressList =
                userAddressMapper.selectList(new QueryWrapper<UserAddress>().eq("user_id", userId));
        return userAddressList;
    }
}
