package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class SpuInfoImplService extends ServiceImpl<SpuInfoMapper, SpuInfo> implements SpuInfoService {
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        spuInfoMapper.insert(spuInfo);
        //所以这里是可以直接获取更新后的Id
        Long spuInfoId = spuInfo.getId();
        //保存SpuSaleAttrList 涉及两张表 spuSaleAttr 和spuSaleValue
        for(SpuSaleAttr spuSaleAttr:spuSaleAttrList){
            String saleAttrName = spuSaleAttr.getSaleAttrName();
            spuSaleAttr.setSpuId(spuInfoId);
            spuSaleAttrMapper.insert(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for(SpuSaleAttrValue spuSaleAttrValue:spuSaleAttrValueList)
            {
                spuSaleAttrValue.setSaleAttrName(saleAttrName);
                spuSaleAttrValue.setSpuId(spuInfoId);
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }
        //保存图片
        for (SpuImage spuImage:spuImageList){
            spuImage.setSpuId(spuInfoId);
            spuImageMapper.insert(spuImage);
        }
    }
}
