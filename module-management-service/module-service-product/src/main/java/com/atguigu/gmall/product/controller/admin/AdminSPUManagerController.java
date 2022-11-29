package com.atguigu.gmall.product.controller.admin;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.service.SpuSaleAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("admin/product")
public class AdminSPUManagerController {

    @Autowired
    SpuInfoService spuInfoService;
    @Autowired
    SpuSaleAttrService spuSaleAttrService;
    @Autowired
    SpuSaleAttrValueService spuSaleAttrValueService;
    @Autowired
    SpuImageService spuImageService;
    @GetMapping("/{pageNum}/{pageSize}")
    public Result<IPage> getPageList(@PathVariable("pageNum") Long pageNum,
                                     @PathVariable("pageSize") Long pageSize,
                                     @RequestParam("category3Id") Long category3Id)
    {
        IPage<SpuInfo> iPage = new Page<>();
        iPage.setCurrent(pageNum);
        iPage.setSize(pageSize);
        QueryWrapper<SpuInfo> spuInfoQueryWrapper= new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",category3Id);
        IPage<SpuInfo> spuInfoIPage = spuInfoService.page(iPage, spuInfoQueryWrapper);
        return  Result.ok(spuInfoIPage);
    }


    @PostMapping("saveSpuInfo")
    public Result<Void> saveSpuInfo(@RequestBody SpuInfo spuInfo){

        //insert操作会更新实体的id
        spuInfoService.saveSpuInfo(spuInfo);


        return Result.ok();
    }


}
