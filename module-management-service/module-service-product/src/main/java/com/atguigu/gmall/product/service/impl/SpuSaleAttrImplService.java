package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.aopcache.GmallCache;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * spu销售属性 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class SpuSaleAttrImplService extends ServiceImpl<SpuSaleAttrMapper, SpuSaleAttr> implements SpuSaleAttrService {
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
       List<SpuSaleAttr>  spuSaleAttrList=spuSaleAttrMapper.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    @GmallCache(prefix = "sal")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
       List<SpuSaleAttr> spuSaleAttrList=spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrList;
    }
}
