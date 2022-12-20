package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RabbitConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.mapper.CartMapper;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    //- detailArrayList：List<OrderDetail>集合
    //- totalNum：当前结算页中商品总数量
    //- totalAmount：当前结算页中商品总金额
    //- userAddressList：List<UserAddress>集合
    //一个orderInfo 对应多个OrderDetail
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    UserFeignClient userFeignClient;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    CartMapper cartMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RabbitService rabbitService;

    @Override
    public Map<String, Object> getTradeData(Long userId) {
        //将查到的购物车数据设置到 OrderDetail中
        List<CartInfo> checkedCart = cartFeignClient.getCheckedCart(userId).getData();
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : checkedCart) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cartInfo, orderDetail);
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetailList.add(orderDetail);
        }
        //商品数量
        Integer totalNum =
                checkedCart
                        .stream()
                        .map(CartInfo::getSkuNum)
                        .reduce(Integer::sum)
                        .get();
        //商品总金额
        Double totalAmount = checkedCart.stream().map(cartInfo -> cartInfo.getSkuNum() * cartInfo.getSkuPrice().doubleValue()).reduce(Double::sum).get();
        //地址列表
        List<UserAddress> userAddressList = userFeignClient.getUserAddressList(userId).getData();
        Map<String, Object> summaryMap = new HashMap<>();

        summaryMap.put("detailArrayList", orderDetailList);
        summaryMap.put("totalNum", totalNum);
        summaryMap.put("totalAmount", totalAmount);
        summaryMap.put("userAddressList", userAddressList);
        return summaryMap;
    }

    @Override
    public Long saveOrder(OrderInfo orderInfo, Long userId) {
        //设置订单信息
        //传过来的 orderInfo 中 只有 paymentWay:"ONLINE" orderDetailLis
        //                        this.order.consignee = item.consignee
        //                        this.order.consigneeTel = item.phoneNum
        //                        this.order.deliveryAddress = item.userAddress
        //                        this.order.paymentWay = this.paymentWay
        setOrderInfoData(orderInfo, userId);
        //保存订单 获取自增主键
        orderInfoMapper.insert(orderInfo);
        Long id = orderInfo.getId();
        //保存订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(id);
            orderDetailMapper.insert(orderDetail);
        }
        //删除 购物车选中数据  删除购物车数据按理来说应该是要调用购物车模块
        //但是不在 两个模块不在一个事务里面 所以在这个模块中使用cart mapper
        cartMapper.delete(new QueryWrapper<CartInfo>().eq("user_id", userId).eq("is_checked", 1));
        //删除Redis 中的数据
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;

        Object[] skuArry = orderDetailList.stream().map(orderDetail -> orderDetail.getSkuId().toString())
                .collect(Collectors.toList())
                .toArray();
        //这里可以传可变参数所以就可以 使用传数组进去
        redisTemplate.boundHashOps(cartKey).delete(skuArry);
        //返回orderInfo的主键Id
        return id;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        return orderInfo;

    }

    @Override
    public void closeOrder(Long orderId) {
        //关闭订单
        orderDetailMapper.deleteById(orderId);
        orderInfoMapper.deleteById(orderId);
    }

    @Override
    public OrderInfo getOrderInfoByOutTradeNo(String outTradeNo) {
        return orderInfoMapper.selectOne(new QueryWrapper<OrderInfo>().eq("out_trade_no", outTradeNo));

    }

    @Override
    public void updateById(OrderInfo orderInfo) {

        orderInfoMapper.updateById(orderInfo);
    }

    @Override
    public void notifyWareSystemToDeliver(Long orderId) {
        //更改订单状态 为通知库存系统
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        orderInfo.setOrderStatus(ProcessStatus.NOTIFIED_WARE.name());
        orderInfo.setProcessStatus(ProcessStatus.NOTIFIED_WARE.name());
        orderInfoMapper.updateById(orderInfo);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        orderInfo.setOrderDetailList(orderDetailList);
        String messageContent = generateOrderJSON(orderInfo);
        //Mq发送消息通知
        rabbitService.sendMessage(RabbitConst.EXCHANGE_DIRECT_WARE_STOCK, RabbitConst.ROUTING_WARE_STOCK, messageContent);
    }

    @Override
    public List<JSONObject> doOrderSplit(Long orderId, String wareSkuMapListJson) {
        //拆分订单
        //先拿到主订单 需要拿到orderDetailLsit的数据
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        orderInfo.setOrderDetailList(orderDetailList);
        //解析库存系统发来的数据
        //：List<Map<String, Object>> 类型
        // List 数据结构：集合元素是 Map 类型
        // Map 的键是：wareId 值是仓库 id
        // Map 的键是skuId 的值是：JSONObject，其中包含当前仓库内所有商品的 skuId 组成的集合
        // 拆分思路：每一个仓库对应一个子订单，子订单需要保存到 order_info 表，然后拼装库存系统所需数据
        List<Map<String, Object>> wareSkuMapList = JSONObject.parseObject(wareSkuMapListJson, List.class);
        //遍历wareSkuMapList 拿到每一个map 从map中拿到仓库id 对应的skuId的集合
        List<JSONObject> resultList = wareSkuMapList.stream().map(wareSkuMap -> {
            //获取仓库Id
            String wareId = (String) wareSkuMap.get("wareId");
            // 获取skuId
            JSONArray skuIds = (JSONArray) wareSkuMap.get("skuIds");
            //生成子订单并保存 返回子订单
            OrderInfo subOrderInfo = parseAndGenerateSubOrder(orderId, orderInfo, skuIds);
            //创建JSONObject 封装子订单对象
            JSONObject jsonObjectSub = new JSONObject();
            // 把子订单对象中封装的属性值存入 JSONObject
            jsonObjectSub.put("orderBody", subOrderInfo.getTradeBody());
            jsonObjectSub.put("consignee", subOrderInfo.getConsignee());
            jsonObjectSub.put("orderComment", subOrderInfo.getOrderComment());
            jsonObjectSub.put("wareId", wareId);
            jsonObjectSub.put("orderId", subOrderInfo.getId());
            jsonObjectSub.put("deliveryAddress", subOrderInfo.getDeliveryAddress());

            List<JSONObject> subDetailList = subOrderInfo.getOrderDetailList()
                    .stream()
                    .map(orderDetail -> {
                        JSONObject jsonObjectDetail = new JSONObject();
                        jsonObjectDetail.put("skuId", orderDetail.getSkuId());
                        jsonObjectDetail.put("skuName", orderDetail.getSkuName());
                        jsonObjectDetail.put("skuNum", orderDetail.getSkuNum());
                        return jsonObjectDetail;
                    }).collect(Collectors.toList());
            jsonObjectSub.put("details", subDetailList);

            return jsonObjectSub;
        }).collect(Collectors.toList());
        //更改母单状态
        orderInfo.setOrderStatus(OrderStatus.SPLIT.name());
        orderInfo.setProcessStatus(ProcessStatus.SPLIT.name());
        orderInfoMapper.updateById(orderInfo);
        return resultList;
    }

    @Override
    public void updateOrderStatus(Long orderId, String orderStatus, String processStatus) {
        orderInfoMapper.updateOrderStatus(orderId, orderStatus, processStatus);
    }

    private OrderInfo parseAndGenerateSubOrder(Long orderId, OrderInfo orderInfo, JSONArray skuIds) {
        //创建子订单封装数据
        OrderInfo subOrderInfo = new OrderInfo();
        //母单数据 和 子单数据相同 赋值
        BeanUtils.copyProperties(orderInfo, subOrderInfo);

        subOrderInfo.setId(null);
        subOrderInfo.setParentOrderId(orderId);
        //使用流处理拿到 在skuIds 中的商品数据
        List<OrderDetail> subOrderDetailList = orderInfo.getOrderDetailList()
                .stream()
                .filter(orderDetail ->
                        skuIds.contains(orderDetail.getSkuId().toString())
                ).collect(Collectors.toList());
        subOrderInfo.setOrderDetailList(subOrderDetailList);

        setOrderInfoData(subOrderInfo,orderInfo.getUserId());
        orderInfoMapper.insert(subOrderInfo);
        //获取自增主键
        Long id = subOrderInfo.getId();

        subOrderInfo.getOrderDetailList()
                .stream()
                .forEach(orderDetail -> {
                    orderDetail.setOrderId(id);
                    orderDetailMapper.insert(orderDetail);
                });
        return subOrderInfo;
    }

    private String generateOrderJSON(OrderInfo orderInfo) {
// 1、创建 JSONObject 对象
        JSONObject jsonObject = new JSONObject();

        // 2、拼接 OrderInfo 对象本身的数据
        jsonObject.put("orderId", orderInfo.getId());
        jsonObject.put("consignee", orderInfo.getConsignee());
        jsonObject.put("consigneeTel", orderInfo.getConsigneeTel());
        jsonObject.put("orderComment", orderInfo.getOrderComment());
        jsonObject.put("orderBody", orderInfo.getTradeBody());
        jsonObject.put("deliveryAddress", orderInfo.getDeliveryAddress());

        // ※特殊注意：库存系统中 ware_order_task 表 payment_way 字段长度只有 2，不能存 ONLINE 这个字符串
        switch (orderInfo.getPaymentWay()) {
            case "ONLINE":
                // 在线支付
                jsonObject.put("paymentWay", "2");
                break;
            default:
                // 货到付款
                jsonObject.put("paymentWay", "1");
        }


        // 3、拼接级联 Detail 部分
        List<JSONObject> detailJsonList = orderInfo.getOrderDetailList()
                .stream()
                .map(orderDetail -> {
                    Long skuId = orderDetail.getSkuId();
                    String skuName = orderDetail.getSkuName();
                    Integer skuNum = orderDetail.getSkuNum();

                    JSONObject jsonObjectDetail = new JSONObject();
                    jsonObjectDetail.put("skuId", skuId);
                    jsonObjectDetail.put("skuName", skuName);
                    jsonObjectDetail.put("skuNum", skuNum);

                    return jsonObjectDetail;
                }).collect(Collectors.toList());

        jsonObject.put("details", detailJsonList);

        return jsonObject.toJSONString();
    }

    //这里传的值是对象地址 所以可以直接修改对象不需要返回
    private void setOrderInfoData(OrderInfo orderInfo, Long userId) {
        //总金额
        orderInfo.sumTotalAmount();
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.toString());
        //用户Id
        orderInfo.setUserId(userId);
        //订单号 订单号是唯一的 所以我们需要根据一套规则指定公司名称 + 时间戳 + 随机数
        //根据订单号访问的时候url上面的：会转义所以换成_
        String outTradeNo = "MUCH_MONEY_" + userId + "_" + System.currentTimeMillis() + new Random().nextInt(1000);
        //订单描述 所有的列表商品拼接 ，而成
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        StringBuilder builder = new StringBuilder();
        orderDetailList.stream().forEach(orderDetail -> {
            builder.append(orderDetail.getSkuName()).append(",");
        });
        String tradeBody = builder.toString();
        //因为最后一个字符是，所以要去掉
        tradeBody.substring(0, tradeBody.lastIndexOf(",") - 1);
        if (tradeBody.length() > 150) {
            //防止过长
            tradeBody = tradeBody.substring(0, 150);
        }
        orderInfo.setTradeBody(tradeBody);
        orderInfo.setOutTradeNo(outTradeNo);
        //创建时间
        orderInfo.setCreateTime(new Date());
        //失效时间 一天
        Calendar currentTime = Calendar.getInstance();
        currentTime.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(currentTime.getTime());
        //进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.toString());
    }


}
