package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.aopcache.GmallCache;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * sku销售属性值 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class SkuSaleAttrValueImplService extends ServiceImpl<SkuSaleAttrValueMapper, SkuSaleAttrValue> implements SkuSaleAttrValueService {

    @Override
    @GmallCache(prefix = "svm")
    public Map getSkuValueIdsMap(Long spuId) {
        Map<String, String> hashMap = new HashMap<>();
        List<Map<String, String>> skuValueIdsMap =baseMapper.getSkuValueIdsMap(spuId);
        skuValueIdsMap.forEach(map -> {
           hashMap.put(map.get("sale_attr_value_id"),map.get("sku_id"));
        });
        return hashMap;
    }
}
