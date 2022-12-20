package com.atguigu.gmall.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:alipay.properties")
public class AliPayConfig {
    //ali的地址
    @Value("${alipay_url}")
    private String alipay_url;
    //私钥
    //关于公钥和私钥的问题 公钥加密 私钥解密
    // 我们有一个支付宝的公钥 私钥是只有支付宝有 所以可以解密我们发送的数据
    // 同时我们还有一个自己的私钥 是支付宝颁发非我们的 对应的公钥在支付宝中 支付宝返回的数据只有我们的私钥能够解密
    @Value("${app_private_key}")
    private String app_private_key;
    //你申请后ali给你的应用ID
    @Value("${app_id}")
    private String app_id;


    public final static String format="json";
    public final static String charset="utf-8";
    public final static String sign_type="RSA2";

    // static 修饰的成员变量不能使用 @Value 注解修饰，所以下面通过 setXxx() 方法设置
    //同步回调地址
    public static String return_payment_url;
    //异步回调的得知
    public static String notify_payment_url;
    //返回支付成功后跳转的订单地址
    public static String return_order_url;
    //公钥
    public static String alipay_public_key;

    @Value("${alipay_public_key}")
    public void setAlipay_public_key(String alipay_public_key) {
        AliPayConfig.alipay_public_key = alipay_public_key;
    }

    @Value("${return_payment_url}")
    public void setReturn_url(String return_payment_url) {
        AliPayConfig.return_payment_url = return_payment_url;
    }

    @Value("${notify_payment_url}")
    public void setNotify_url(String notify_payment_url) {
        AliPayConfig.notify_payment_url = notify_payment_url;
    }

    @Value("${return_order_url}")
    public void setReturn_order_url(String return_order_url) {
        AliPayConfig.return_order_url = return_order_url;
    }

    // <bean id="alipayClient" class="com.alipay.api.AlipayClient"></bean>
    @Bean
    public AlipayClient alipayClient(){
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayClient alipayClient=new DefaultAlipayClient(alipay_url,app_id,app_private_key,format,charset, alipay_public_key,sign_type );
        return alipayClient;
    }

}
