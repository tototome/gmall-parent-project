package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategory3;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 三级分类表 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface BaseCategory3Service extends IService<BaseCategory3> {

    List<BaseCategory3> getBaseCategory3(Long baseCategory2Id);
}
