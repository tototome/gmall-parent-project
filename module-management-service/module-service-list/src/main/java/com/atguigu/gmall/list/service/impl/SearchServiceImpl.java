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
        //使用zset 里面是只有score 方便保存我们的热度值
        String hotScoreKey = "hot:key";
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //zSetOperations操作zset
        Double aDouble = zSetOperations.incrementScore(hotScoreKey, "skuId:" + skuId, 1);

        if (aDouble % 10 == 0) {
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            //设置热度值
            goods.setHotScore(aDouble.longValue());
            goodsRepository.save(goods);
        }

    }

    @Override
    public SearchResponseVo doSearch(SearchParam searchParam) {

        try {
            //构建DSL语句
            SearchRequest searchRequest = this.buildQueryDSL(searchParam);
            //执行搜索
            SearchResponse searchResponse = highLevelClient.search(searchRequest);
            //解析searchResponse 获得结果封装到VO中
            SearchResponseVo searchResponseVo = parseSearchResponse(searchResponse);
            // 给 SearchResponseVo 对象设置分页参数
            searchResponseVo.setPageNo(searchParam.getPageNo());
            searchResponseVo.setPageSize(searchParam.getPageSize());

            searchResponseVo.setTotalPages((long)Math.ceil((searchResponseVo.getTotal())/ searchResponseVo.getPageSize()));
            //返回解析后的对象
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    private SearchRequest buildQueryDSL(SearchParam searchParam) {
        //创建一个DSL查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建query.bool构造器
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //构建query中must部分
        String keyWord = searchParam.getKeyword();
        //判断关键词是否为空
        if (!StringUtils.isEmpty(keyWord)) {
            //title是会进行分词的  所以两个次是一起查的 Operator.AND 相当于Must
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyWord)
                    .operator(Operator.AND);
            //相当于query.bool.must.match
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //构建query中的filter部分 平台属性和分类ID
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
        //filter中有平台属性 和 品牌使用的是nested
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = StringUtils.split(trademark, ":");
            String tmId = split[0];
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", tmId));
        }
        //设置平台属性

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
        //完成query的装配
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        int from = ((searchParam.getPageNo() - 1) * searchParam.getPageSize());
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title")
                .preTags("<span style='color:red'>")
                .postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //排序
        String order = searchParam.getOrder();
        if (order != null&&order.length()>0) {
            String[] split = order.split(":");
            String orderType = split[0];
            String orderBy = split[1];
            String filedName = "1".equals(orderType) ? "hotScore" : "price";
            SortOrder sortOrder = "ASC".equals(orderBy.toUpperCase()) ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(orderType, sortOrder);
        }
        //聚合
        //根据品牌ID聚合（普通数据类型）桶的名字
        TermsAggregationBuilder tmAggregationBuilder = AggregationBuilders.terms("tmIdAgg")
                .field("tmId")
                //子聚合 方便拿数据
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

        //平台属性聚合 nested类型数据
        TermsAggregationBuilder attrsAggregationBuilder = AggregationBuilders.terms("attrIdAgg")
                .field("attrs.attrId")
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        //外层
        NestedAggregationBuilder nestedAttrAgg = AggregationBuilders
                .nested("attrAgg", "attrs")
                .subAggregation(attrsAggregationBuilder);
        //组装聚合
        searchSourceBuilder.aggregation(nestedAttrAgg);
        searchSourceBuilder.aggregation(tmAggregationBuilder);

        //指定字段值
        searchSourceBuilder.fetchSource(new String[]{"id", "title", "defaultImg", "price"}, null);
        //封装搜索请求对象
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        //设置查询对象
        searchRequest.source(searchSourceBuilder);

        System.out.println(searchRequest.source());

        return searchRequest;
    }

    //响应数据结构解析
    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        List<Goods> goodsList = new ArrayList<>();
        //获得条件匹配到的数据
        SearchHits hits = searchResponse.getHits();
        //可以看下测试数据的查询结果 数据结构是这样的
        // "hits" : {
        //    "total" : 6,
        //    "max_score" : 1.0,
        //    "hits" : []
        SearchHit[] hitsArray = hits.getHits();
        //遍历赋值
        for (int i = 0; hitsArray != null && i < hitsArray.length; i++) {
            SearchHit searchHit = hitsArray[i];
            String sourceAsString = searchHit.getSourceAsString();
            //解析字符串 一个json的字符串
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //获取高亮数据
            HighlightField highlightField = searchHit.getHighlightFields().get("title");
            if (highlightField!=null){
                Text title= highlightField.getFragments()[0];
                goods.setTitle(title.toString());
            }
            goodsList.add(goods);
        }

        //获取聚合数据
        Aggregations aggregations = searchResponse.getAggregations();
        Map<String, Aggregation> stringAggregationMap = aggregations.asMap();
        //聚合的命名
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) stringAggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> searchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            String tmId = bucket.getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(tmId));
            // 聚合里面还有 品牌logo聚合 和品牌名聚合 方便取数据
            Map<String, Aggregation> tmIdAggMap = bucket.getAggregations().asMap();
            //获取品牌名的聚合数据
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdAggMap.get("tmNameAgg");
            //每一个品牌id只有一个品牌名
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            // 获取品牌 Logo 的聚合数据
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdAggMap.get("tmLogoUrlAgg");

            // 因为我们是先根据 tmId 做的聚合，所以在 tmId 下，tmLogoUrl 保证只有一个值
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(searchResponseTmVoList);
        //设置平台属性
        //获得根据attrId聚合结果
        ParsedNested attrAgg = (ParsedNested) stringAggregationMap.get("attrAgg");
         ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            List<SearchResponseAttrVo> searchResponseAttrVoList = buckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //设置平台属性值
                searchResponseAttrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
                List<? extends Terms.Bucket> attrNameBuckets = attrNameAgg.getBuckets();
                String attrName = attrNameBuckets.get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                //设置平台属性值
                ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                List<String> valueList = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(valueList);
                return searchResponseAttrVo;

            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(searchResponseAttrVoList);
        }
        // 获取总记录数
        searchResponseVo.setTotal(hits.totalHits);
        searchResponseVo.setGoodsList(goodsList);
        return searchResponseVo;
    }
}
