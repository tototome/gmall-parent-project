package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class BaseTrademarkImplService extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {

    @Override
    public IPage<BaseTrademark> getPageList(Integer pageNum, Integer limit) {
        IPage<BaseTrademark>  iPage= new Page<>();
        iPage.setCurrent(pageNum);
        iPage.setSize(limit);
        IPage<BaseTrademark> iPage1 = baseMapper.selectPage(iPage, null);
        return iPage1;
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {

        return  baseMapper.selectList(null);

    }
}
