package com.atguigu.gmall.item.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequestMapping("api/item")
@RestController
public class ItemController {
    @Autowired
    ItemService itemService;


    @GetMapping("/{skuId}")
    public Result<Map<String, Object>> getItem(@PathVariable("skuId") Long skuId) throws ExecutionException, InterruptedException {

        Result<Map<String, Object>> dataMap = itemService.getItem(skuId);

        return dataMap;

    }

}
