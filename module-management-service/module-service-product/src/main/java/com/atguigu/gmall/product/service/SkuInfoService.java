package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 库存单元表 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface SkuInfoService extends IService<SkuInfo> {

    void saveSkuInfo(SkuInfo skuInfo);

    IPage<SkuInfo> getList(Long pageNum, Long size);

    SkuInfo getSkuInfo(Long skuId);

    BigDecimal getSkuPrice(Long skuId);
}
