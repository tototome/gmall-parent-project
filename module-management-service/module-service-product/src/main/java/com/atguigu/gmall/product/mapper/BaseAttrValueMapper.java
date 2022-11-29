package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 属性值表 Mapper 接口
 * </p>
 * com.atguigu.gmall.product
 *
 */
public interface BaseAttrValueMapper extends BaseMapper<BaseAttrValue> {

    void insertBatch(@Param("attrId") Long attrId,@Param("attrValueList") List<BaseAttrValue> attrValueList);
}
