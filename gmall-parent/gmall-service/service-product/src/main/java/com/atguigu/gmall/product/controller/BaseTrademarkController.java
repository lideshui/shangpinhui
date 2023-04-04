package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import com.atguigu.gmall.product.service.BaseTrademarkService;




import org.springframework.web.bind.annotation.RestController;

/**
 * 品牌表 前端控制器
 */
@Api(tags = "品牌表控制器")
@RestController
@RequestMapping("/admin/product/baseTrademark") //TODO 填写基础映射URL
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;



    //品牌管理分页接口
    @GetMapping("/{page}/{limit}")
    public Result baseTrademarkByPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit
    ){
        //1.封装分页请求参数Page - 分页对象只有页大小跟页码
        IPage<BaseTrademark> iPage = new Page<>(page, limit);
        iPage = baseTrademarkService.baseTrademarkByPage(iPage);
        return Result.ok(iPage);
    }


    //品牌新增
    @PostMapping("/save")
    public Result SaveBaseTrademark(
            @RequestBody BaseTrademark baseTrademark
    ){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    //根据品牌id获取品牌
    @GetMapping("/get/{id}")
    public Result<BaseTrademark> getBaseTrademarkById(
            @PathVariable("id") Long id
    ){
        BaseTrademark trademark = baseTrademarkService.getById(id);
        return Result.ok(trademark);
    }

    //修改品牌
    @PutMapping("/update")
    public Result editBaseTrademark(
            @RequestBody BaseTrademark baseTrademark
    ){
        //注意方法是updateById
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    //删除品牌
    @DeleteMapping("/remove/{id}")
    public Result deleteBaseTrademarkById(
            @PathVariable("id") Long id
    ){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
}
