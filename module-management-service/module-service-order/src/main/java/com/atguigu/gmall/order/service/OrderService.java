package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

public interface OrderService  {
    Map<String, Object> getTradeData(Long userId);

    Long saveOrder(OrderInfo orderInfo, Long userId);

    OrderInfo getOrderInfo(Long orderId);

    void closeOrder(Long orderId);

    OrderInfo getOrderInfoByOutTradeNo(String outTradeNo);

    void updateById(OrderInfo orderInfo);

    void notifyWareSystemToDeliver(Long orderId);

     List<JSONObject> doOrderSplit(Long orderId, String wareSkuMapListJson);

    void updateOrderStatus(Long orderId, String toString, String toString1);
}
