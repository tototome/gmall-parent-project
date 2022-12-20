package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    AlipayClient alipayClient;
    @Override
    public void updatePaymentInfoByCallback(String outTradeNo, String paymentType, Map<String, String> paramMap) {
        PaymentInfo paymentInfo = new PaymentInfo();
        //trade_no支付宝的流水号
        paymentInfo.setTradeNo(paramMap.get("trade_no"));
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(paramMap.toString());

        paymentMapper.update(paymentInfo,new QueryWrapper<PaymentInfo>().eq("out_trade_no", outTradeNo)
                .eq("payment_type", paymentType));

    }

    @Override
    public PaymentInfo getPaymentInfoByOutTradeNo(String outTradeNo, String name) {
        PaymentInfo paymentInfo = paymentMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("out_trade_no", outTradeNo)
                .eq("payment_type", name));

        return paymentInfo;
    }

    @Override
    public Boolean


    checkAliPaymentStatus(String outTradeStatus) {
        try {
            // 1、创建请求对象
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

            // 2、封装请求参数
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", outTradeStatus);

            request.setBizContent(bizContent.toString());

            // 3、执行请求获得响应
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            // 4、返回查询结果
            return response.isSuccess();
        } catch (AlipayApiException e) {
            e.printStackTrace();

            return false;
        }
    }


    @Override
    public Boolean closeAliPayTrade(String outTradeNo) {

        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", outTradeNo);
            request.setBizContent(bizContent.toString());
            AlipayTradeCloseResponse response = alipayClient.execute(request);

            return response.isSuccess();
        } catch (AlipayApiException e) {
            e.printStackTrace();

            return false;
        }
    }

    @Override
    public void closePayRecordLocal(String outTradeNo) {

        // 1、查询 PaymentInfo 对象
        PaymentInfo paymentInfo = paymentMapper.selectOne(
                new QueryWrapper<PaymentInfo>().eq("out_trade_no", outTradeNo));

        // 2、如果 PaymentInfo 对象存在并且是未支付状态
        // ※支付状态都转换成字符串类型才可以进行比较
        // ※经验：比较两个数据是否相等时，一定要先调整到同一个类型才能比较
        if (paymentInfo != null && PaymentStatus.UNPAID.name().equals(paymentInfo.getPaymentStatus())) {

            // 3、关闭 PaymentInfo
            paymentInfo.setPaymentStatus(PaymentStatus.ClOSED.name());
            paymentMapper.updateById(paymentInfo);
        }

    }

    @Override
    public PaymentInfo selectPaymentInfoByOrderIdAndPayType(String orderId, String paymentType) {
        PaymentInfo paymentInfo = paymentMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("order_id", orderId)
                .eq("payment_type", paymentType));

        return paymentInfo;
    }

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setCreateTime(new Date());
        paymentMapper.insert(paymentInfo);
    }
}
