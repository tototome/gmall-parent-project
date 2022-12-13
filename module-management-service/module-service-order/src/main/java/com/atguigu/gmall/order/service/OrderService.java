package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

import java.util.Map;

public interface OrderService  {
    Map<String, Object> getTradeData(Long userId);

    Long saveOrder(OrderInfo orderInfo, Long userId);

    OrderInfo getOrderInfo(Long orderId);

    void closeOrder(Long orderId);
}
