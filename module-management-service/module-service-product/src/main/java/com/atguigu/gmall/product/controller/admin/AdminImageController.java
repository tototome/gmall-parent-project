package com.atguigu.gmall.product.controller.admin;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.product.service.SpuImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/product/")
public class AdminImageController {

    @Autowired
    SpuImageService spuImageService;

    @GetMapping("spuImageList/{spuId}")
    public Result<List<SpuImage>> getImageList(@PathVariable("spuId") Long spuId) {
        List<SpuImage> spuImageList = spuImageService.getImageList(spuId);
        return Result.ok(spuImageList);

    }
}
