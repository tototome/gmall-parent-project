package com.atguigu.gmall.cart;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.atguigu.gmall")
@EnableFeignClients(basePackages = "com.atguigu.gmall")
public class GMallServiceCartMainType {
    public static void main(String[] args) {
        SpringApplication.run(GMallServiceCartMainType.class,args);
    }
}
