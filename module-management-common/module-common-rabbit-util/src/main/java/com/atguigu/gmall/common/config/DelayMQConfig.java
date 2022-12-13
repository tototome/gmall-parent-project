package com.atguigu.gmall.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Configuration
//延迟插件配置
public class DelayMQConfig {

    public static final String APPLE_EXCHANGE_DELAY = "exchange.delay.apple";
    public static final String APPLE_ROUTING_KEY_DELAY = "routing.key.delay.apple";
    public static final String APPLE_QUEUE_DELAY = "queue.delay.apple";

    @Bean
    public CustomExchange getCustomExchange(){
        // 1、创建 Map 集合用于封装交换机参数
        Map<String, Object> argumentsMap = new HashMap<>();

        // 2、设置创建交换机的参数
        argumentsMap.put("x-delayed-type", "direct");

        // 3、声明交换机类型
        String type = "x-delayed-message";

        // 4、创建交换机对象
        return new CustomExchange(
                APPLE_EXCHANGE_DELAY,
                type,
                true,
                false,
                argumentsMap);
    }
    //创建延迟队列
    @Bean
    public Queue getQueue(){
        return new Queue(APPLE_QUEUE_DELAY,true);
    }

    //绑定队列
    @Bean
    public Binding getBind(){

        return BindingBuilder.bind(getQueue()).to(getCustomExchange()).with(APPLE_ROUTING_KEY_DELAY).noargs();
    }
}
