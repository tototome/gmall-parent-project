package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

public interface SearchService {
    void importGoodsToElasticSearch(Long skuId);

    void removeGoodsFromElasticSearch(Long skuId);

    void incrHotScore(Long skuId);

    SearchResponseVo doSearch(SearchParam searchParam);
}
