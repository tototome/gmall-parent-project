package com.atguigu.gmall.product.controller.admin;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/admin/product/baseTrademark")
public class AdminTradeMarkController {

    @Autowired
    BaseTrademarkService baseTrademarkService;
    @GetMapping("{page}/{limit}")
    public Result<IPage<BaseTrademark>> getPageList(@PathVariable("page") Integer pageNum, @PathVariable("limit") Integer limit){
        IPage<BaseTrademark> baseTrademarkIPage =baseTrademarkService.getPageList(pageNum,limit);
       return Result.ok(baseTrademarkIPage);
    }

    @DeleteMapping("remove/{id}")
    public Result<Void> removeById(@PathVariable("id") Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    @PostMapping("save")
    public Result<Void> save(@RequestBody BaseTrademark baseTrademark){

        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    @GetMapping("get/{id}")
    public  Result<BaseTrademark> getById(@PathVariable("id") Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return  Result.ok(baseTrademark);
    }

    @PutMapping("update")
    public Result<Void> updateById(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    @GetMapping("getTrademarkList")
    public  Result<List<BaseTrademark>> getTrademarkList(){
       List<BaseTrademark> baseTrademarkList=baseTrademarkService.getTrademarkList();
       return Result.ok(baseTrademarkList);
    }

}
