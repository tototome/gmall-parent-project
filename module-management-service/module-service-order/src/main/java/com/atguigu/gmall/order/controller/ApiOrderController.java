package com.atguigu.gmall.order.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RabbitConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/order")
public class ApiOrderController {
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    OrderService orderService;
    @Autowired
    RedissonClient redissonClient;
    @Value("${ware.url}")
    String wareUrl;
    @Autowired
    RabbitService rabbitService;

    //组装 订单数据
    @GetMapping("/inner/auth/trade")
    public Result<Map<String, Object>> trad(@RequestHeader("userId") Long userId) {
        if (StringUtils.isEmpty(userId)) {
            return Result.<Map<String, Object>>fail().message("没有获取到有效 userId！");
        }

        Map<String, Object> tradeData = orderService.getTradeData(userId);

        return Result.ok(tradeData);
    }

    //post 请求 请求体是order
    @PostMapping("/auth/submitOrder")
    public Result<Long> submitOrder(@RequestBody OrderInfo orderInfo, @RequestHeader("userId") Long userId) {
        //加上分布式锁 防止订单重复提交
        String lockKey = RedisConst.ORDER_SUBMIT_LOCK_KEY_PREFIX + userId + RedisConst.ORDER_SUBMIT_LOCK_KEY_SUFFIX;
        //获取锁对象
        RLock lock = redissonClient.getLock(lockKey);
        //尝试加锁
        boolean lockResult = lock.tryLock();
        if (lockResult) {
            //加上锁
            try {
                //数据校验 价格是否变动 库存是否还有
                List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
                for (OrderDetail orderDetail : orderDetailList) {
                    BigDecimal orderPrice = orderDetail.getOrderPrice();
                    BigDecimal currentPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId()).getData();
                    if (!orderPrice.equals(currentPrice)) {
                        return Result.<Long>fail().message("价格已发生变化！请重新加入购物车。" + orderDetail.getSkuName());
                    }
                }
                //检查库存
                for (OrderDetail orderDetail : orderDetailList) {
                    //远程访问库存系统的地址
                    String accessPath =
                            wareUrl + "/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum();
                    //httpClient 发起请求
                    String checkResult = HttpClientUtil.doGet(accessPath);
                    if ("0".equals(checkResult)) {
                        return Result.<Long>fail().message("商品库存不足！" + orderDetail.getSkuName());
                    }
                }
                //返回值是orderId
                Long orderId = orderService.saveOrder(orderInfo, userId);
                //发送延迟消息 30分钟后没有支付取消订单
                rabbitService.sendDelayMessage(RabbitConst.EXCHANGE_DIRECT_ORDER_CANCEL,RabbitConst.ROUTING_ORDER_CANCEL,orderId,RabbitConst.Delay_Time);
                return Result.ok(orderId);
            } catch (Exception e) {
                return Result.<Long>fail().message("下单出错，异常信息：" + e.getMessage());
            } finally {
                //最终要解锁
                lock.unlock();
            }
        } else {
            return Result.<Long>fail().message("您已经提交的订单我们正在拼命处理中，请勿重复提交订单！");
        }

    }

    @GetMapping("/auth/getPayOrderInfo/{orderId}")
    public Result<Map<Object, Object>> getPayOrderInfoById(@PathVariable("orderId") Long orderId) {
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        String outTradeNo = orderInfo.getOutTradeNo();
        BigDecimal totalAmount = orderInfo.getTotalAmount();

        Map<Object, Object> resultMap = new HashMap<>();
        resultMap.put("id", outTradeNo);
        resultMap.put("totalAmount", totalAmount);
        return Result.ok(resultMap);
    }
    @GetMapping("/auth/getOrderInfoById/{orderId}")
    public Result<OrderInfo> getOrderInfoById(@PathVariable("orderId") Long orderId){
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return  Result.ok(orderInfo);
    }
    @GetMapping("/auth/getOrderInfoByOutTradeNo/{outTradeNo}")
    public Result<OrderInfo> OrderInfoByOutTradeNo(@PathVariable("outTradeNo")String outTradeNo){
        OrderInfo orderInfo=orderService.getOrderInfoByOutTradeNo(outTradeNo);
        return Result.ok(orderInfo);
    };
    @PostMapping("/orderSplit")
    public List<JSONObject> doOrderSplit(@RequestParam("orderId") Long orderId, @RequestParam("wareSkuMap") String wareSkuMapListJson) {

        return orderService.doOrderSplit(orderId, wareSkuMapListJson);
    }
}
