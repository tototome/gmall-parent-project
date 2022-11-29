package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 基本销售属性表 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface BaseSaleAttrService extends IService<BaseSaleAttr> {

    List<BaseSaleAttr> getBaseSaleAttrList();
}
