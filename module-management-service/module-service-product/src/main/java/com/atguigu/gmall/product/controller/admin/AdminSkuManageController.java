package com.atguigu.gmall.product.controller.admin;

import com.atguigu.gmall.common.constant.RabbitConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product/")
public class AdminSkuManageController {

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    RabbitService rabbitService;
    @PostMapping("saveSkuInfo")
    public Result<Void> saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuInfoService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @GetMapping("list/{pageNum}/{size}")
    public  Result<IPage<SkuInfo>> getList(@PathVariable("pageNum") Long pageNum, @PathVariable("size")Long size){

       IPage<SkuInfo> iPage =skuInfoService.getList(pageNum,size);
       return Result.ok(iPage);
    }

    @GetMapping("onSale/{skuId}")
    public Result<Void> onSale(@PathVariable("skuId") Long skuId){
        SkuInfo skuById = skuInfoService.getById(skuId);
        skuById.setIsSale(1);
        skuInfoService.updateById(skuById);
        //上架后通知ES 修改数据
        // 发送消息：执行 ElasticSearch 数据装入
        rabbitService.sendMessage(RabbitConst.RABBIT_EXCHANGE_GOODS, RabbitConst.RABBIT_ROUTING_KEY_GOODS_IMPORT, skuId);
        return Result.ok();
    }

    @GetMapping("cancelSale/{skuId}")
    public Result<Void> cancelSale(@PathVariable("skuId") Long skuId){
        SkuInfo skuById = skuInfoService.getById(skuId);
        skuById.setIsSale(0);
        skuInfoService.updateById(skuById);
        // 发送消息：执行 ElasticSearch 数据删除
        rabbitService.sendMessage(RabbitConst.RABBIT_EXCHANGE_GOODS, RabbitConst.RABBIT_ROUTING_KEY_GOODS_REMOVE, skuId);
        return Result.ok();
    }
}
