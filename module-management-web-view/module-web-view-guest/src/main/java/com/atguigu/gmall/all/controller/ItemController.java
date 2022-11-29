package com.atguigu.gmall.all.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

//@RestController  视图解析不要使用Restroller 因为Restroller里面是有@ResponseBody 直接返回 那串字符了 ！！！！
@Controller
public class ItemController {

    @Autowired
    ItemFeignClient itemFeignClient;


    @RequestMapping("/{skuId}.html")
    public String getItem(@PathVariable("skuId") Long id, Model model) throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> item = itemFeignClient.getItem(id).getData();

        //错误定位 绑定失败 先看传入的数据 有数据看请求域数据model
        model.addAllAttributes(item);
        //model.addAttribute(categoryView);
        return "item/index";
    }

}
