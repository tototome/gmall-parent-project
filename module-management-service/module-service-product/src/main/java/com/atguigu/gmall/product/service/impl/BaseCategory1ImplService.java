package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.service.BaseCategory1Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 一级分类表 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class BaseCategory1ImplService extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategory1Service {
    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;

    @Override
    public List<BaseCategory1> getBaseCategory1() {
        List<BaseCategory1> baseCategory1List = baseCategory1Mapper.selectList(new QueryWrapper<>());
        return  baseCategory1List;
    }


}
