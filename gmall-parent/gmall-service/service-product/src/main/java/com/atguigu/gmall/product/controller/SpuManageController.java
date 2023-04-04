package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.model.BaseSaleAttr;
import com.atguigu.gmall.product.model.SpuInfo;
import com.atguigu.gmall.product.service.SpuManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/product")
public class SpuManageController {

    @Autowired
    private SpuManageService spuManageService;


    //分页查询商品SPU列表，接收查询参数设置required默认true，如果不传的话就报错了⚠️
    @GetMapping("/{page}/{size}")
    public Result getSpuByPage(@PathVariable("page") Long page, @PathVariable("size") Long size, @RequestParam(value = "category3Id", required = false) Long category3Id){
        //创建分页对象
        IPage<SpuInfo> infoPage = new Page<>(page, size);
        //传递分页对象和搜索参数
        infoPage = spuManageService.getSpuByPage(infoPage, category3Id);
        return Result.ok(infoPage);
    }


    //查询所有销售属性，创建SKU时候的下拉列表要用
    @GetMapping("/baseSaleAttrList")
    public Result<List<BaseSaleAttr>> baseSaleAttrList(){
        List<BaseSaleAttr> list = spuManageService.getBaseSaleAttrList();
        return Result.ok(list);
    }


    //创建商品SPU信息，创建时要对spu属性、图片、海报、销售属性、销售属性值进行赋值
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuManageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }



}
