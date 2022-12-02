package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.aopcache.GmallCache;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 库存单元表 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class SkuInfoImplService extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;


    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

        skuInfoMapper.insert(skuInfo);
        Long skuInfoId = skuInfo.getId();
        Long spuId = skuInfo.getSpuId();
        //这里本应该用 动态sql 批量 添加 偷懒了
        for (SkuAttrValue skuAttrValue:skuAttrValueList){
            skuAttrValue.setSkuId(skuInfoId);
            skuAttrValueMapper.insert(skuAttrValue);
        }

        for (SkuSaleAttrValue skuSaleAttrValue:skuSaleAttrValueList){
            skuSaleAttrValue.setSkuId(skuInfoId);
            skuSaleAttrValue.setSpuId(spuId);
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }

        for (SkuImage skuImage:skuImageList){
            skuImage.setSkuId(skuInfoId);
            skuImageMapper.insert(skuImage);
        }


    }

    @Override
    public IPage<SkuInfo> getList(Long pageNum, Long size) {
        IPage<SkuInfo> iPage = new Page<>();
        iPage.setCurrent(pageNum);
        iPage.setSize(size);
        IPage<SkuInfo> iPage1 = baseMapper.selectPage(iPage, new QueryWrapper<SkuInfo>().orderByDesc("id"));
        return iPage1;
    }

    @Override
    @GmallCache(prefix = "sku")
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
      BigDecimal  skuPrice =baseMapper.getSkuPrice(skuId);
      return  skuPrice;
    }


}
