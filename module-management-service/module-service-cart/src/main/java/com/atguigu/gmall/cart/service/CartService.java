package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartService {
    void addCart(Long skuId, Integer skuNum, String userId);

    List<CartInfo> selectCart(String userId, String userTempId);

    void modifyCartCheckStatus(String userId, Long skuId, Integer isChecked);

    void removeCartItem(String userId, Long skuId);
}
