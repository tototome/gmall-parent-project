package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping(value = {"/","/index.html"})
    public String getIndex(Model model){
        List<JSONObject> allCategoryForPortal = productFeignClient.getAllCategoryForPortal().getData();
        model.addAllAttributes(allCategoryForPortal);
        return "index";
    }
}
