package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.common.constant.RabbitConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AliPayConfig;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/payment/alipay")
public class ApiPaymentController {

    @Autowired
    //这里在配置类中我们配置过了
    AlipayClient alipayClient;
    @Autowired
    RabbitService rabbitService;
    @Autowired
    OrderFeignClient orderFeignClient;
    @Autowired
    PaymentInfoService paymentInfoService;

    @ResponseBody
    @GetMapping("/submit/{outTradeNo}")
    public String alipaySubmitToCodePage(@PathVariable("outTradeNo") String outTradeNo) throws AlipayApiException {
        OrderInfo orderInfo = orderFeignClient.OrderInfoByOutTradeNo(outTradeNo).getData();
        //判断订单状态
        if (!orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.name())) {
            return "当前订单不需要支付";
        }
        String paymentType = PaymentType.ALIPAY.name();
        //查询paymentInfo信息 根据订单Id
        PaymentInfo paymentInfo = paymentInfoService.selectPaymentInfoByOrderIdAndPayType(outTradeNo, paymentType);
        if (paymentInfo == null) {
            //支付信息为空则重新保存一个
            paymentInfoService.savePaymentInfo(orderInfo, paymentType);
        }


        //设置请求 注意接口使用的类
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //同步回调 给用户看的页面
        alipayRequest.setReturnUrl(AliPayConfig.return_payment_url);
        //异步回调
        alipayRequest.setNotifyUrl(AliPayConfig.notify_payment_url);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", orderInfo.getTotalAmount());
        bizContent.put("subject", orderInfo.getTradeBody());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        alipayRequest.setBizContent(bizContent.toString());

        AlipayTradePagePayResponse alipayTradeQueryResponse = alipayClient.pageExecute(alipayRequest);
        String body = alipayTradeQueryResponse.getBody();
        return body;

    }


    @GetMapping("/callback/return")
    public String syncCallback() {
        //http://payment.gmall.com/pay/success.html
        return "redirect:" + AliPayConfig.return_order_url;
    }

    //支付宝文档地址 https://opendocs.alipay.com/open/270/105902
    @PostMapping("callback/notify")
    @ResponseBody
    public String asyncCallback(@RequestParam Map<String, String> paramMap) throws AlipayApiException {
        boolean signVerified = AlipaySignature.rsaCertCheckV1(paramMap, AliPayConfig.alipay_public_key, AliPayConfig.charset, AliPayConfig.sign_type);
        if (!signVerified) {
            //验签失败
            return "failure";
        }
        //检验交易状态
        String tradeStatus = paramMap.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "failure";
        }
        //检查数据库的数据防止 出现错误
        String outTradeNo = paramMap.get("out_trade_no");
        PaymentInfo paymentInfo = paymentInfoService.selectPaymentInfoByOrderIdAndPayType(outTradeNo, PaymentType.ALIPAY.name());

        String paymentStatus = paymentInfo.getPaymentStatus();
        if (OrderStatus.PAID.equals(paymentStatus) || OrderStatus.CLOSED.name().equals(paymentStatus)) {
            return "dailure";
        }
        //确保无误 更新数据库保存返回的信息
        paymentInfoService.updatePaymentInfoByCallback(outTradeNo, PaymentType.ALIPAY.name(), paramMap);
        //异步通知修改 订单表 这里不使用fegin的原因是 异步通知失败可以重试 由于网络原因等...... 异步通知有百分百消息确认机制
        rabbitService.sendMessage(RabbitConst.EXCHANGE_DIRECT_PAYMENT_ORDER_UPDATE,
                                    RabbitConst.ROUTING_PAYMENT_ORDER_UPDATE,paymentInfo.getOrderId());

        return "success";
    }

    @ResponseBody
    @GetMapping("/get/payment/info/{outTradeNo}")
    public Result<PaymentInfo> getPaymentInfoByOutTradeNo(@PathVariable("outTradeNo") String outTradeNo) {
        return Result.ok(paymentInfoService.getPaymentInfoByOutTradeNo(outTradeNo, PaymentType.ALIPAY.name()));
    }

    @ResponseBody
    @GetMapping("/check/ali/pay/status/{outTradeStatus}")
    public Result<Boolean> checkAliPaymentStatus(@PathVariable("outTradeStatus") String outTradeStatus) {
        return Result.ok(paymentInfoService.checkAliPaymentStatus(outTradeStatus));
    }

    @ResponseBody
    @GetMapping("/close/ali/pay/trade/{outTradeNo}")
    public Result<Boolean> closeAliPayTrade(@PathVariable("outTradeNo") String outTradeNo) {
        return Result.ok(paymentInfoService.closeAliPayTrade(outTradeNo));
    }
    @ResponseBody
    @GetMapping("/close/pay/record/local/{outTradeNo}")
    public Result<Void> closePayRecordLocal(@PathVariable("outTradeNo") String outTradeNo) {

        paymentInfoService.closePayRecordLocal(outTradeNo);

        return Result.ok();
    }

}
