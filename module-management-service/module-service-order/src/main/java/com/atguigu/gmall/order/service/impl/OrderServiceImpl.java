package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
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

    //这里传的值是对象地址 所以可以直接修改对象不需要返回
    private void setOrderInfoData(OrderInfo orderInfo, Long userId) {
        //总金额
        orderInfo.sumTotalAmount();
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.toString());
        //用户Id
        orderInfo.setUserId(userId);
        //订单号 订单号是唯一的 所以我们需要根据一套规则指定公司名称 + 时间戳 + 随机数
        String outTradeNo = "MUCH_MONEY:" + userId + ":" + System.currentTimeMillis() + new Random().nextInt(1000);
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
