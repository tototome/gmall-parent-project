package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter getCorsWebFilter() {
        // CORS跨域配置对象
        CorsConfiguration configuration = new CorsConfiguration();

        // 设置允许访问的网络
        configuration.addAllowedOrigin("*");

        // 设置是否从服务器获取cookie
        configuration.setAllowCredentials(true);

        // 设置请求方法 * 表示任意
        configuration.addAllowedMethod("*");

        // 允许携带请求头信息 * 表示任意
        configuration.addAllowedHeader("*");

        // 配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);

        // CORS过滤器对象
        return new CorsWebFilter(configurationSource);
    }
}
