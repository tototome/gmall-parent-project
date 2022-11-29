package com.atguigu.gmall.product.controller.admin;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;

import com.atguigu.gmall.product.service.*;
import com.atguigu.gmall.product.util.GmallUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/admin/product")
public class AdminManageController {
    @Value("${fileServer.url}")
    private String fileServerUrl;
    @Autowired
    BaseCategory1Service baseCategory1Service;
    @Autowired
    BaseCategory2Service baseCategory2Service;
    @Autowired
    BaseCategory3Service baseCategory3Service;
    @Autowired
    BaseAttrInfoService baseAttrInfoService;
    @Autowired
    SpuSaleAttrService spuSaleAttrService;
    @Autowired
    BaseSaleAttrService baseSaleAttrService;

    @GetMapping("/getBaseCategory1")
    public Result<List<BaseCategory1>> getBaseCategory1() {
        List<BaseCategory1> baseCategory1List = baseCategory1Service.getBaseCategory1();
        return Result.ok(baseCategory1List);
    }

    @GetMapping("/getBaseCategory2/{baseCategory1Id}")
    public Result<List<BaseCategory2>> getBaseCategory2(@PathVariable("baseCategory1Id") Long baseCategory1Id) {
        List<BaseCategory2> baseCategory2List = baseCategory2Service.getBaseCategory2(baseCategory1Id);
        return Result.ok(baseCategory2List);
    }

    @GetMapping("/getBaseCategory3/{baseCategory2Id}")
    public Result<List<BaseCategory3>> getBaseCategory3(@PathVariable("baseCategory2Id") Long baseCategory2Id) {
        List<BaseCategory3> baseCategory3List = baseCategory3Service.getBaseCategory3(baseCategory2Id);
        return Result.ok(baseCategory3List);
    }

    //获取平台属性
    @GetMapping("getAttrInfoList/{category1}/{category2}/{category3}")
    public Result<List<BaseAttrInfo>> getAttrInfoList(@PathVariable("category1") Long category1, @PathVariable("category2") Long category2,
                                                      @PathVariable("category3") Long category3
    ) {
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoService.getAttrInfoList(category1, category2, category3);
        return Result.ok(baseAttrInfoList);
    }

    @PostMapping("saveAttrInfo")
    public Result<Void> saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        baseAttrInfoService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    @GetMapping("deleteById/{id}")
    public Result<Void> deleteById(@PathVariable("id") Long id) {
        baseAttrInfoService.deleteById(id);
        return Result.ok();
    }

    @PostMapping("fileUpload")
    public Result<String> fileUpload(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            Result<String> result=Result.fail();
            result.setMessage("空文件");
            return  result;
        }
        String uploadUrl = GmallUploadUtil.doUpload(multipartFile.getOriginalFilename(), multipartFile.getBytes());
        uploadUrl=fileServerUrl+uploadUrl;
        return Result.ok(uploadUrl);
    }

    @GetMapping("baseSaleAttrList")
    public Result<List<BaseSaleAttr>> baseSaleAttrList(){
       List<BaseSaleAttr>  baseSaleAttrList=baseSaleAttrService.getBaseSaleAttrList();
       return  Result.ok(baseSaleAttrList);
    }

    @GetMapping("spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId")Long spuId){
      List<SpuSaleAttr> spuSaleAttrList= spuSaleAttrService.getSpuSaleAttrList(spuId);
      return Result.ok(spuSaleAttrList);
    }


}
