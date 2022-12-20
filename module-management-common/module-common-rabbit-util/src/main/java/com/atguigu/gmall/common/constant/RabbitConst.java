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
    public static final String EXCHANGE_DIRECT_PAYMENT_ORDER_UPDATE="exchange.payment.order.update";
    public static final String ROUTING_PAYMENT_ORDER_UPDATE="routingKey.payment.order.update";
    public static final String QUEUE_PAYMENT_ORDER_UPDATE="queue.payment.order.update";

    /**
     * 减库存
     */
    public static final String EXCHANGE_DIRECT_WARE_STOCK = "exchange.direct.ware.stock";
    public static final String ROUTING_WARE_STOCK = "ware.stock";
    //队列
    public static final String QUEUE_WARE_STOCK  = "queue.ware.stock";

    /**
     * 减库存成功，更新订单状态
     */
    public static final String EXCHANGE_DIRECT_WARE_ORDER = "exchange.direct.ware.order";
    public static final String ROUTING_WARE_ORDER = "ware.order";
    //队列
    public static final String QUEUE_WARE_ORDER  = "queue.ware.order";
    //定时任务
    public static final String EXCHANGE_DIRECT_TASK_SECKILL="exchange.direct.task.seckill";
    public static final String ROUTING_TASK_SECKILL="routingkey.task.seckill";
    public static final String QUEUE_TASK_SECKILL="queue.task.seckill";
}
