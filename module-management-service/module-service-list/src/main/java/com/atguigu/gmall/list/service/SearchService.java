package com.atguigu.gmall.list.service;

public interface SearchService {
    void importGoodsToElasticSearch(Long skuId);

    void removeGoodsFromElasticSearch(Long skuId);

    void incrHotScore(Long skuId);
}
