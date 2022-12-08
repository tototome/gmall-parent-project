package com.atguigu.gmall.user;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.atguigu.gmall")
public class GMallServiceUserMainType {
    public static void main(String[] args) {
        SpringApplication.run(GMallServiceUserMainType.class,args);
    }

}
