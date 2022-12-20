package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentInfoService {
    PaymentInfo selectPaymentInfoByOrderIdAndPayType(String orderId, String paymentType);

    void savePaymentInfo(OrderInfo orderInfo, String paymentType);

    void updatePaymentInfoByCallback(String outTradeNo, String paymentType, Map<String, String> paramMap);

    PaymentInfo getPaymentInfoByOutTradeNo(String outTradeNo, String name);

    Boolean checkAliPaymentStatus(String outTradeStatus);

    Boolean closeAliPayTrade(String outTradeNo);

    void closePayRecordLocal(String outTradeNo);
}
