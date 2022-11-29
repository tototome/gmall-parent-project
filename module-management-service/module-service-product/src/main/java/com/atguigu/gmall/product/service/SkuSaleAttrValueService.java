package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * sku销售属性值 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValue> {

    Map getSkuValueIdsMap(Long spuId);
}
