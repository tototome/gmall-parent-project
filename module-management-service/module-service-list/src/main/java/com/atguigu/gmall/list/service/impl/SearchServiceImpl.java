package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.auditing.CurrentDateTimeProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {


    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    // High level REST client that wraps an instance of the low level RestClient
    // and allows to build requests and read responses.
    // The RestClient instance is internally built based on the provided RestClientBuilder
    // and it gets closed automatically when closing the RestHighLevelClient instance that wraps it.
    RestHighLevelClient highLevelClient;

    @Override
    public void importGoodsToElasticSearch(Long skuId) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId).getData();
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId).getData();
        BaseTrademark byId = productFeignClient.getById(skuInfo.getTmId()).getData();
        BaseCategoryView data = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id()).getData();
        Goods goods = new Goods();

        BeanUtils.copyProperties(skuInfo, goods);
        BeanUtils.copyProperties(byId, goods);
        BeanUtils.copyProperties(data, goods);
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());
        goods.setTmLogoUrl(byId.getLogoUrl());
        List<SearchAttr> attrs = new ArrayList<>();
        for (BaseAttrInfo baseAttrInfo : attrList) {
            SearchAttr searchAttr = new SearchAttr();
            BeanUtils.copyProperties(baseAttrInfo, searchAttr);
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            searchAttr.setAttrId(baseAttrInfo.getAttrValueList().get(0).getAttrId());
            attrs.add(searchAttr);
        }
        goods.setId(skuId);
        goods.setAttrs(attrs);

        goodsRepository.save(goods);
    }

    @Override
    public void removeGoodsFromElasticSearch(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //??????zset ???????????????score ??????????????????????????????
        String hotScoreKey = "hot:key";
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //zSetOperations??????zset
        Double aDouble = zSetOperations.incrementScore(hotScoreKey, "skuId:" + skuId, 1);

        if (aDouble % 10 == 0) {
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            //???????????????
            goods.setHotScore(aDouble.longValue());
            goodsRepository.save(goods);
        }

    }

    @Override
    public SearchResponseVo doSearch(SearchParam searchParam) {

        try {
            //??????DSL??????
            SearchRequest searchRequest = this.buildQueryDSL(searchParam);
            //????????????
            SearchResponse searchResponse = highLevelClient.search(searchRequest);
            //??????searchResponse ?????????????????????VO???
            SearchResponseVo searchResponseVo = parseSearchResponse(searchResponse);
            // ??? SearchResponseVo ????????????????????????
            searchResponseVo.setPageNo(searchParam.getPageNo());
            searchResponseVo.setPageSize(searchParam.getPageSize());

            searchResponseVo.setTotalPages((long)Math.ceil((searchResponseVo.getTotal())/ searchResponseVo.getPageSize()));
            //????????????????????????
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    private SearchRequest buildQueryDSL(SearchParam searchParam) {
        //????????????DSL?????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //??????query.bool?????????
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //??????query???must??????
        String keyWord = searchParam.getKeyword();
        //???????????????????????????
        if (!StringUtils.isEmpty(keyWord)) {
            //title?????????????????????  ?????????????????????????????? Operator.AND ?????????Must
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyWord)
                    .operator(Operator.AND);
            //?????????query.bool.must.match
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //??????query??????filter?????? ?????????????????????ID
        Long category1Id = searchParam.getCategory1Id();
        if (!StringUtils.isEmpty(category1Id)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", category1Id));
        }
        Long category2Id = searchParam.getCategory2Id();
        if (!StringUtils.isEmpty(category2Id)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", category2Id));
        }
        Long category3Id = searchParam.getCategory3Id();
        if (!StringUtils.isEmpty(category3Id)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", category3Id));
        }
        //filter?????????????????? ??? ??????????????????nested
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = StringUtils.split(trademark, ":");
            String tmId = split[0];
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", tmId));
        }
        //??????????????????

        String[] props = searchParam.getProps();
        if (props!=null&&props.length>0) {
            for (String prop : props) {
                String[] split = prop.split(":");

                if (split != null && split.length == 3) {
                    String attrId = split[0];
                    String attrValue = split[1];
                    BoolQueryBuilder attrBoolQuery = QueryBuilders.boolQuery();
                    attrBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                    attrBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", attrValue));
                    NestedQueryBuilder nestedQuery = new NestedQueryBuilder("attrs", attrBoolQuery, ScoreMode.None);
                    boolQueryBuilder.filter(nestedQuery);
                }
            }
        }
        //??????query?????????
        searchSourceBuilder.query(boolQueryBuilder);
        //??????
        int from = ((searchParam.getPageNo() - 1) * searchParam.getPageSize());
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());
        //??????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title")
                .preTags("<span style='color:red'>")
                .postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //??????
        String order = searchParam.getOrder();
        if (order != null&&order.length()>0) {
            String[] split = order.split(":");
            String orderType = split[0];
            String orderBy = split[1];
            String filedName = "1".equals(orderType) ? "hotScore" : "price";
            SortOrder sortOrder = "ASC".equals(orderBy.toUpperCase()) ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(orderType, sortOrder);
        }
        //??????
        //????????????ID??????????????????????????????????????????
        TermsAggregationBuilder tmAggregationBuilder = AggregationBuilders.terms("tmIdAgg")
                .field("tmId")
                //????????? ???????????????
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

        //?????????????????? nested???????????? ?????????xxx.xxx ??????Nest?????????????????????
        TermsAggregationBuilder attrsAggregationBuilder = AggregationBuilders.terms("attrIdAgg")
                .field("attrs.attrId")
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        //??????
        NestedAggregationBuilder nestedAttrAgg = AggregationBuilders
                .nested("attrAgg", "attrs")
                .subAggregation(attrsAggregationBuilder);
        //????????????
        searchSourceBuilder.aggregation(nestedAttrAgg);
        searchSourceBuilder.aggregation(tmAggregationBuilder);

        //???????????????
        searchSourceBuilder.fetchSource(new String[]{"id", "title", "defaultImg", "price"}, null);
        //????????????????????????
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        //??????????????????
        searchRequest.source(searchSourceBuilder);

        System.out.println(searchRequest.source());

        return searchRequest;
    }

    //????????????????????????
    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        List<Goods> goodsList = new ArrayList<>();
        //??????????????????????????????
        SearchHits hits = searchResponse.getHits();
        //??????????????????????????????????????? ????????????????????????
        // "hits" : {
        //    "total" : 6,
        //    "max_score" : 1.0,
        //    "hits" : []
        SearchHit[] hitsArray = hits.getHits();
        //????????????
        for (int i = 0; hitsArray != null && i < hitsArray.length; i++) {
            SearchHit searchHit = hitsArray[i];
            String sourceAsString = searchHit.getSourceAsString();
            //??????????????? ??????json????????????
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //??????????????????
            HighlightField highlightField = searchHit.getHighlightFields().get("title");
            if (highlightField!=null){
                Text title= highlightField.getFragments()[0];
                goods.setTitle(title.toString());
            }
            goodsList.add(goods);
        }

        //??????????????????
        Aggregations aggregations = searchResponse.getAggregations();
        Map<String, Aggregation> stringAggregationMap = aggregations.asMap();
        //???????????????
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) stringAggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> searchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            String tmId = bucket.getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(tmId));
            // ?????????????????? ??????logo?????? ?????????????????? ???????????????
            Map<String, Aggregation> tmIdAggMap = bucket.getAggregations().asMap();
            //??????????????????????????????
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdAggMap.get("tmNameAgg");
            //???????????????id?????????????????????
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            // ???????????? Logo ???????????????
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdAggMap.get("tmLogoUrlAgg");

            // ???????????????????????? tmId ???????????????????????? tmId ??????tmLogoUrl ?????????????????????
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(searchResponseTmVoList);
        //??????????????????
        //????????????attrId????????????
        ParsedNested attrAgg = (ParsedNested) stringAggregationMap.get("attrAgg");
         ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            List<SearchResponseAttrVo> searchResponseAttrVoList = buckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //?????????????????????
                searchResponseAttrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
                List<? extends Terms.Bucket> attrNameBuckets = attrNameAgg.getBuckets();
                String attrName = attrNameBuckets.get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                //?????????????????????
                ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                List<String> valueList = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(valueList);
                return searchResponseAttrVo;

            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(searchResponseAttrVoList);
        }
        // ??????????????????
        searchResponseVo.setTotal(hits.totalHits);
        searchResponseVo.setGoodsList(goodsList);
        return searchResponseVo;
    }
}
