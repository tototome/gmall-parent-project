package com.atguigu.gmall.pay;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.payment.PaymentInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-feign")
public interface PaymentFeignClient {

    @GetMapping("/api/payment/alipay/get/payment/info/{outTradeNo}")
    Result<PaymentInfo> getPaymentInfoByOutTradeNo(@PathVariable("outTradeNo") String outTradeNo);

    @GetMapping("/api/payment/alipay/check/ali/pay/status/{outTradeStatus}")
    Result<Boolean> checkAliPaymentStatus(@PathVariable("outTradeStatus") String outTradeStatus);

    @GetMapping("/api/payment/alipay/close/ali/pay/trade/{outTradeNo}")
    Result<Boolean> closeAliPayTrade(@PathVariable("outTradeNo") String outTradeNo);

    @GetMapping("/close/pay/record/local/{outTradeNo}")
    Result<Void> closePayRecordLocal(@PathVariable("outTradeNo") String outTradeNo);
}