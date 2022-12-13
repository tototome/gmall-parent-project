package com.atguigu.gmall.common.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 我们这里创建这个组件，那么将来在项目中，任何地方需要发送消息就直接调用sendMessage()方法即可。
 *
 * @author: 封捷
 * @create-date: 2022/9/9 10:30
 */
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 把消息发送到消息队列时调用的方法
     *
     * @param exchange   指定交换机的名称
     * @param routingKey 指定和某一个队列对应的路由 key
     * @param message    当前要发送的消息
     * @return 消息是否发送成功
     * true：成功
     * false：失败
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {

        try {
            // 调用模板方法发送消息
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            return true;
        } catch (AmqpException e) {
            e.printStackTrace();
            return false;
        }
    }

    //发送延迟消息的方法
    public boolean sendDelayMessage(String exchange, String routingKey, Object message, int delayTime) {

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, messageProcessor -> {
                messageProcessor.getMessageProperties().setDelay(delayTime * 1000);
                return messageProcessor;
            });
            return true;
        } catch (AmqpException e) {
            e.printStackTrace();
            return false;
        }
    }

}

