package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @RequestMapping("/trade.html")
    public String trade(Model model) {

        Map<String, Object> tradeMap = orderFeignClient.trad().getData();

        model.addAllAttributes(tradeMap);

        return "order/trade";
    }

    @RequestMapping("/pay.html")
    public String pay(@RequestParam("orderId") Long orderId, Model model) {
        model.addAttribute("orderId", orderId);

        return "order/success";

    }


}
