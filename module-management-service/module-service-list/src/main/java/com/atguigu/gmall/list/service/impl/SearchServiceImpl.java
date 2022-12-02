package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.auditing.CurrentDateTimeProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SearchServiceImpl implements SearchService {


    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public void importGoodsToElasticSearch(Long skuId) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId).getData();
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId).getData();
        BaseTrademark byId = productFeignClient.getById(skuInfo.getTmId()).getData();
        BaseCategoryView data = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id()).getData();
        Goods goods = new Goods();

        BeanUtils.copyProperties(skuInfo,goods);
        BeanUtils.copyProperties(byId,goods);
        BeanUtils.copyProperties(data,goods);
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());
        goods.setTmLogoUrl(byId.getLogoUrl());
       List<SearchAttr> attrs =new ArrayList<>();
       for (BaseAttrInfo baseAttrInfo:attrList){
           SearchAttr searchAttr = new SearchAttr();
           BeanUtils.copyProperties(baseAttrInfo,searchAttr);
           searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
           attrs.add(searchAttr);
       }
       goods.setAttrs(attrs);

       goodsRepository.save(goods);
    }
    @Override
    public void removeGoodsFromElasticSearch(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //使用zset 里面是只有score 方便保存我们的热度值
        String hotScoreKey="hot:key";
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //zSetOperations操作zset
        Double aDouble = zSetOperations.incrementScore(hotScoreKey, "skuId:" + skuId, 1);

        if (aDouble%10==0){
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            //设置热度值
            goods.setHotScore(aDouble.longValue());
            goodsRepository.save(goods);
        }

    }
}
