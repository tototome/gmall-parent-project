package com.atguigu.gmall.order.receiver;


import com.atguigu.gmall.common.constant.RabbitConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Configuration
public class OrderReceiver {

    @Autowired
    OrderService orderService;

    //监听消息 前面在配延迟配置中 我们已经给 队列绑定了 交换机
    //只需要监听指定队列就可以了
    @RabbitListener(queues = RabbitConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel) throws IOException {

        try {
            OrderInfo orderInfo = orderService.getOrderInfo(orderId);
            String orderStatus = orderInfo.getOrderStatus();
            String processStatus = orderInfo.getProcessStatus();
            if (orderStatus.equals("UNPAID") && processStatus.equals("UNPAID")) {
                orderService.closeOrder(orderId);
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
}
