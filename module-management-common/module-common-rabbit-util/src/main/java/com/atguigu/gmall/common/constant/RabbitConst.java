package com.atguigu.gmall.common.constant;

public class RabbitConst {
    public static final String RABBIT_EXCHANGE_GOODS = "exchange.goods";
    public static final String RABBIT_ROUTING_KEY_GOODS_IMPORT = "routingKey.goods.import";
    public static final String RABBIT_ROUTING_KEY_GOODS_REMOVE = "routingKey.goods.remove";

    public static final String EXCHANGE_DIRECT_ORDER_CANCEL = "exchange.delay.order";
    public static final String QUEUE_ORDER_CANCEL = "queue.delay.order";
    public static final String ROUTING_ORDER_CANCEL = "routingKey.order.cancel";
    public static final Integer Delay_Time=60*30;
    public static final Integer Delay_Time_Test=30;
}
