package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 属性表 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class BaseAttrInfoImplService extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo> implements BaseAttrInfoService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {

       List<BaseAttrInfo> baseAttrInfoList =baseAttrInfoMapper.getAttrInfoList(category1Id,category2Id,category3Id);


        return baseAttrInfoList;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //快捷键ctrl+alt+v 提取变量
        Long id = baseAttrInfo.getId();
        if (id ==null|| id.equals("")){
            baseMapper.insert(baseAttrInfo);

            id=baseAttrInfo.getId();
        }else {
            baseMapper.updateById(baseAttrInfo);
            baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>()
                    .eq("attr_id", id));
        }
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        baseAttrValueMapper.insertBatch(id,attrValueList);

    }

    @Override
    public void deleteById(Long id) {
        baseMapper.deleteById(id);
        baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>().eq("attr_id",id));
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {

        return   baseMapper.getAttrList(skuId);

    }


}
