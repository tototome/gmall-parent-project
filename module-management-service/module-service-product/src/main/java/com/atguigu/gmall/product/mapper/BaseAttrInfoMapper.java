package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 属性表 Mapper 接口
 * </p>
 * com.atguigu.gmall.product
 *
 */
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    List<BaseAttrInfo> getAttrInfoList(@Param("category1Id")Long category1Id,@Param("category2Id") Long category2Id, @Param("category3Id")Long category3Id);

    List<BaseAttrInfo> getAttrList(@Param("skuId") Long skuId);
}
