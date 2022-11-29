package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 库存单元表 Mapper 接口
 * </p>
 * com.atguigu.gmall.product
 *
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    BigDecimal getSkuPrice(@Param("skuId") Long skuId);
}
