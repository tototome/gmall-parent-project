package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.service.BaseCategory2Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 二级分类表 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class BaseCategory2ImplService extends ServiceImpl<BaseCategory2Mapper, BaseCategory2> implements BaseCategory2Service {

    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;
    @Override
    public List<BaseCategory2> getBaseCategory2(Long baseCategory1Id) {
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id",baseCategory1Id);
        List<BaseCategory2> baseCategory2s = baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
        return baseCategory2s;
    }
}
