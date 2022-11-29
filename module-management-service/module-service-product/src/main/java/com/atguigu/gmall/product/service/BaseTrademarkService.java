package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {

    IPage<BaseTrademark> getPageList(Integer pageNum, Integer limit);

    List<BaseTrademark> getTrademarkList();
}
