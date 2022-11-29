package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;


public interface ItemService {
    Result<Map<String, Object>> getItem(Long skuId) throws ExecutionException, InterruptedException;
}
