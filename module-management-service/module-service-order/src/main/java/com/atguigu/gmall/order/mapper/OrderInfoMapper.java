package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    void updateOrderStatus(
            @Param("orderId") Long orderId,
            @Param("orderStatus") String orderStatus,
            @Param("processStatus") String processStatus);


}
