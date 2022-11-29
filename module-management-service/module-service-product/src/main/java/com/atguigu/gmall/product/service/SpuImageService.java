package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品图片表 服务类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
public interface SpuImageService extends IService<SpuImage> {

    List<SpuImage> getImageList(Long spuId);
}
