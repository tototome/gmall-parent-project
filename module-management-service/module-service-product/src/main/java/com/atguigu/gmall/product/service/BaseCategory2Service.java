package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategory2;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 二级分类表 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface BaseCategory2Service extends IService<BaseCategory2> {

    List<BaseCategory2> getBaseCategory2(Long baseCategory1Id);
}
