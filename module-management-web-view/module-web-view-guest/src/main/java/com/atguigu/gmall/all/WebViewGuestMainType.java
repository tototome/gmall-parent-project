package com.atguigu.gmall.all;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.DispatcherServlet;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.atguigu.gmall"})
@ComponentScan(value = {"com.atguigu.gmall.item.client",
        "com.atguigu.gmall.product.client",
        "com.atguigu.gmall.all",
        "com.atguigu.gmall.list.client",
        "com.atguigu.gmall.common.interceptor"})
public class WebViewGuestMainType {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(WebViewGuestMainType.class, args);
        String[] beanDefinitionNames = run.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }
    }

}