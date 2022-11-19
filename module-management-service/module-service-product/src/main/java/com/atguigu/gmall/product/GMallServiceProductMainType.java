package com.atguigu.gmall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(value = "com.atguigu.gmall")
@SpringBootApplication
@EnableDiscoveryClient
public class GMallServiceProductMainType {
    public static void main(String[] args) {
        SpringApplication.run(GMallServiceProductMainType.class, args);
    }
}
