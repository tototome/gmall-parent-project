package com.atguigu.gmall.gateway.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
public class AuthFilter implements GlobalFilter {
    @Autowired
    RedisTemplate redisTemplate;

    @Value("${atguigu.auth.urlPath}")
    String urlPath;

    // 遵循 Ant 风格的路径匹配器
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    // 赋值给 userId 后表示用户未登录
    public static final String USER_NOT_LOGIN = "userNotLogin";

    // 请求路径的匹配模式：匹配内部接口间调用时使用的路径
    public static final String URL_PATTERN_INNER = "/**/inner/**";

    // 请求路径的匹配模式：匹配需要登录后才能访问的异步请求资源
    public static final String URL_PATTERN_AUTH = "/**/auth/**";

    // 作为用户临时 id 的默认值
    public static final String USER_TEMP_ID_NOT_EXISTS = "userTempIdNotExists";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        //获取路径 就是主机名端口号后面的一串
        String path = uri.getPath();
        if (antPathMatcher.match(URL_PATTERN_INNER, path)) {
            //异步拒绝 访问
            return asyncRefuse(exchange);
        }
        if (antPathMatcher.match(URL_PATTERN_AUTH, path)) {
            String checkLogin = isCheckLogin(request);
            if (checkLogin.equals(USER_NOT_LOGIN)) {
                //异步拒绝
                return asyncRefuse(exchange);
            }
            //登陆 设置userId
            return setHeaderUserId(exchange, chain, request, checkLogin);
        }
        //判断 path是否在 pathUrl 里面 trade.html,myOrder.html,list.html
        //由于得到的path是带/····· 所以要去掉/
        int i = path.indexOf("/");
        String substring = path.substring(i + 1);
        if (urlPath.contains(substring) && !substring.equals("")) {
            String checkLogin = isCheckLogin(request);
            if (checkLogin.equals(USER_NOT_LOGIN)) {
                //说明未登录 同步拒绝
                return syncRefuse(exchange);
            }
            //登陆 将userId 设置到请求头中返回
            return setHeaderUserId(exchange, chain, request, checkLogin);

        }

        return setUserTempIdIfExists(exchange, chain);
    }

    private Mono<Void> setHeaderUserId(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest request, String checkLogin) {
        request.mutate().header("userId", checkLogin);
        exchange.mutate().request(request);
        return chain.filter(exchange);
    }

    private Mono<Void> syncRefuse(ServerWebExchange exchange) {
        //同步拒绝 重定向到登陆页面
        ServerHttpResponse response = exchange.getResponse();
        //303 表示跳转到别的页面
        response.setStatusCode(HttpStatus.SEE_OTHER);
        //前端会得到originUrl 后的参数
        String location = "http://passport.gmall.com/login.html?originUrl=" + exchange.getRequest().getURI();
        response.getHeaders().set("location", location);
        return response.setComplete();
    }

    private String isCheckLogin(ServerHttpRequest request) {
        //判断是否登陆
        String token = null;
        String tokenKey = null;
        List<String> list = request.getHeaders().get("token");
        if (list == null) {
            HttpCookie tokenCookie = request.getCookies().getFirst("token");
            if (tokenCookie != null) {
                token = tokenCookie.getValue();
            }
        } else {
            token = list.get(0);
        }
        if (token == null) {
            //未登录 异步拒绝
            return USER_NOT_LOGIN;
        }
        tokenKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        String tokenValue = (String) redisTemplate.opsForValue().get(tokenKey);
        String[] split = tokenValue.split(":");
        String userId = split[0];
        String ipAddress = split[1];
        //  判断ipAddress是否相等
        String gatewayIpAddress = IpUtil.getGatwayIpAddress(request);
        if (!gatewayIpAddress.equals(ipAddress)) {
            //说明用户没有登陆
            return USER_NOT_LOGIN;
        }
        //登陆 将userId 返回userId
        return userId;
    }

    //异步拒绝
    private Mono<Void> asyncRefuse(ServerWebExchange exchange) {
        Result<Void> result = Result.build(null, ResultCodeEnum.PERMISSION);
        String resultJson = JSON.toJSONString(result);
        byte[] bytes = resultJson.getBytes();
        ServerHttpResponse response = exchange.getResponse();
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        response.getHeaders().set("Content-type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }

    //读取临时ID
    private String getUserTempId(ServerHttpRequest request) {

        // 1、从 header 中读取
        List<String> userTempIdHeaderList = request.getHeaders().get("userTempId");
        if (!CollectionUtils.isEmpty(userTempIdHeaderList)) {
            return userTempIdHeaderList.get(0);
        }

        // 2、从 Cookie 中读取
        HttpCookie userTempIdCookie = request.getCookies().getFirst("userTempId");
        if (userTempIdCookie != null) {
            return userTempIdCookie.getValue();
        }

        // 3、如果上面都没有读取到，这里返回不存在
        return USER_TEMP_ID_NOT_EXISTS;
    }

    //设置临时ID
    private Mono<Void> setUserTempIdIfExists(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ※============ 检查用户临时 id 是否存在 ===============
        // 1、读取用户的临时 id
        String userTempId = getUserTempId(exchange.getRequest());
        if (USER_TEMP_ID_NOT_EXISTS.equals(userTempId)) {
            userTempId=null;
        }

        // 2、如果临时 id 存在，则设置到请求消息头
        ServerHttpRequest requestHasBeanSetUserId =
                exchange.getRequest()
                        .mutate()
                        .header("userTempId", userTempId)
                        .build();
        String checkLogin = isCheckLogin(exchange.getRequest());
        if (!checkLogin.equals(USER_NOT_LOGIN)){
            exchange.getRequest().mutate().header("userId",checkLogin).build();
        }

        // 2、把当前请求放行
        return chain.filter(
                exchange.mutate()
                        .request(requestHasBeanSetUserId)
                        .build());

    }
}
