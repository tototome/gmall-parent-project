package com.atguigu.gmall.all.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.cart.client.CartFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CartController {
    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;
    @RequestMapping("/addCart.html")
    public String addCart(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum, Model model){

        // 执行添加购物车操作
        cartFeignClient.addCart(skuId, skuNum);

        // 为了在“添加购物车成功”页面显示结果，查询 SkuInfo 信息
        Result<SkuInfo> skuInfoResult = productFeignClient.getSkuInfo(skuId);

        SkuInfo skuInfo = skuInfoResult.getData();

        // 存入模型
        model.addAttribute("skuInfo", skuInfo);
        model.addAttribute("skuNum", skuNum);
        return "cart/addCart";
    }
    @RequestMapping("/cart.html")
    public String toShowCartPage() {
        return "cart/index";
    }
}
