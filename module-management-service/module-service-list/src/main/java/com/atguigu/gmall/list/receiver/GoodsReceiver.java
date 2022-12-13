package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.constant.RabbitConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.io.IOException;

@Slf4j
@Component
@Configuration
public class GoodsReceiver {
    @Autowired
    SearchService searchService;

    //偷偷抛出异常 hhhh @SneakyThrows

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "queue.import.goods220608",
            durable = "true", autoDelete = "false"), exchange = @Exchange(value = RabbitConst.RABBIT_EXCHANGE_GOODS
            , durable = "true", autoDelete = "false"), key = {RabbitConst.RABBIT_ROUTING_KEY_GOODS_IMPORT}))
    public void goodsImport(Long skuId, Message message, Channel channel) throws IOException {
        try {
            searchService.importGoodsToElasticSearch(skuId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
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

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "queue.remove.goods220608", durable = "true", autoDelete = "false"),
                    exchange = @Exchange(value = RabbitConst.RABBIT_EXCHANGE_GOODS, durable = "true", autoDelete = "false"),
                    key = {RabbitConst.RABBIT_ROUTING_KEY_GOODS_REMOVE}
            )
    )
    public void processRemove(Long skuId, Message message, Channel channel) throws IOException {
        try {

            // 执行 ElasticSearch 数据删除
            searchService.removeGoodsFromElasticSearch(skuId);

            // 返回 ACK 确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {

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
