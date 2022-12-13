package com.atguigu.gmall.common.config;

import com.atguigu.gmall.common.constant.RabbitConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class OrderCancelMQConfig {

    @Bean
    public CustomExchange orderDelayExchange(){
    //设置交换机参数
        String type="x-delayed-message";
        Map<String, Object> arguments=new HashMap<>();
        arguments.put("x-delayed-type","direct");
        return  new CustomExchange(RabbitConst.EXCHANGE_DIRECT_ORDER_CANCEL,type,true,false,arguments);
    }

    @Bean
    public Queue orderDelayQueue(){
       return new Queue(RabbitConst.QUEUE_ORDER_CANCEL,true);
    }

    @Bean
    public Binding orderExchangeTOQueue(){
        return  BindingBuilder.bind(orderDelayQueue()).to(orderDelayExchange()).with(RabbitConst.ROUTING_ORDER_CANCEL).noargs();
    }
}
