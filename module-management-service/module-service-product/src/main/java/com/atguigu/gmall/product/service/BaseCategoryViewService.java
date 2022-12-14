package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface BaseCategoryViewService extends IService<BaseCategoryView> {

    List<JSONObject> getAllCategoryForPortal();

}
