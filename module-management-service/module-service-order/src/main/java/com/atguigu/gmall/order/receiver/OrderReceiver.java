package com.atguigu.gmall.order.receiver;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RabbitConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.pay.PaymentFeignClient;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@Configuration
public class OrderReceiver {

    @Autowired
    OrderService orderService;
    @Autowired
    private PaymentFeignClient paymentFeignClient;
    //监听消息 前面在配延迟配置中 我们已经给 队列绑定了 交换机
    //只需要监听指定队列就可以了
    @RabbitListener(queues = RabbitConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel) throws IOException {

        try {
            OrderInfo orderInfo = orderService.getOrderInfo(orderId);
            String orderStatus = orderInfo.getOrderStatus();
            String processStatus = orderInfo.getProcessStatus();
            if (orderStatus.equals("UNPAID") && processStatus.equals("UNPAID")) {
                // 5、远程调用支付微服务接口再次确认
                String outTradeNo = orderInfo.getOutTradeNo();
                Boolean checkResult = paymentFeignClient.checkAliPaymentStatus(outTradeNo).getData();
                // 6、如果支付宝平台上交易记录存在
                if (checkResult) {

                    // 7、尝试关闭支付宝平台交易记录
                    Boolean closeResult = paymentFeignClient.closeAliPayTrade(outTradeNo).getData();

                    // 8、如果能够关闭，则说明已支付，则此处流程结束
                    if (!closeResult) {
                        return ;
                    }

                }
                // 9、流程走到这里就是没有支付，需要关闭订单
                orderService.updateOrderStatus(orderId, OrderStatus.CLOSED.toString(), ProcessStatus.CLOSED.toString());

                // 10、调用 Feign 接口关闭 PaymentInfo
                paymentFeignClient.closePayRecordLocal(outTradeNo);

            }

            //签收消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            // 获取当前消息是否已经被消费过
            Boolean redelivered = message.getMessageProperties().getRedelivered();

            if (redelivered) {
                // 如果当前消息已经被消费过，则拒绝，并不重新放入队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);

            } else {

                // 如果当前消息没有被消费过，则返回 NACK 消息，并重新放入队列
                // 从业务的角度来说可以重试一次
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitConst.QUEUE_PAYMENT_ORDER_UPDATE, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitConst.EXCHANGE_DIRECT_PAYMENT_ORDER_UPDATE, durable = "true", autoDelete = "false"),
            key = {RabbitConst.ROUTING_PAYMENT_ORDER_UPDATE}))
    public void upDateOrder(Long orderId, Message message, Channel channel) throws IOException {

        try {
            //更新消息订单状态
            if (orderId != null) {
                OrderInfo orderInfo = orderService.getOrderInfo(orderId);
                orderInfo.setOrderStatus(OrderStatus.PAID.name());
                orderInfo.setProcessStatus(ProcessStatus.PAID.name());
                orderService.updateById(orderInfo);
                //通知库存系统减库存
                orderService.notifyWareSystemToDeliver(orderId);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            // 获取当前消息是否已经被消费过
            Boolean redelivered = message.getMessageProperties().getRedelivered();

            if (redelivered) {
                // 如果当前消息已经被消费过，则拒绝，并不重新放入队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);

            } else {

                // 如果当前消息没有被消费过，则返回 NACK 消息，并重新放入队列
                // 从业务的角度来说可以重试一次
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitConst.QUEUE_WARE_ORDER),
            exchange = @Exchange(value = RabbitConst.EXCHANGE_DIRECT_WARE_ORDER), key = {RabbitConst.ROUTING_WARE_ORDER}))
    public void updateOrderStatusToWaitingDeliver(String jsonMessage, Message message, Channel channel) throws IOException {
        //传递的JSON数据
        // map.put("orderId", wareOrderTask.getOrderId());
        //map.put("status", wareOrderTask.getTaskStatus().toString());
        Map<String, Object> parseObject = JSON.parseObject(jsonMessage, Map.class);
        String status = (String) parseObject.get("status");
        Long orderId = (Long)parseObject.get("orderId");
        if (status.equals("DEDUCTED")){
            OrderInfo orderInfo = orderService.getOrderInfo(orderId);
            orderInfo.setProcessStatus(ProcessStatus.WAITING_DELEVER.name());
            orderInfo.setOrderStatus(ProcessStatus.WAITING_DELEVER.name());
            orderService.updateById(orderInfo);
        }else {
            OrderInfo orderInfo = orderService.getOrderInfo(orderId);
            orderInfo.setProcessStatus(ProcessStatus.STOCK_EXCEPTION.name());
            orderInfo.setOrderStatus(ProcessStatus.STOCK_EXCEPTION.name());
            orderService.updateById(orderInfo);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
