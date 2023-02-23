package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.model.BaseSaleAttr;
import com.atguigu.gmall.product.model.SpuInfo;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.service.SpuManageService;
import com.atguigu.gmall.product.service.impl.SpuInfoServiceImpl;
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


    //分页查询商品SPU列表
    //查询参数category3Id未省略注解的原因是需要设置默认值，required默认true，必须要传
    @GetMapping("/{page}/{size}")
    public Result getSpuByPage(@PathVariable("page") Long page, @PathVariable("size") Long size, @RequestParam(value = "category3Id", required = false) Long category3Id){

        IPage<SpuInfo> infoPage = new Page<>(page, size);
        //传递分页对象和搜索参数
        infoPage = spuManageService.getSpuByPage(infoPage, category3Id);
        return Result.ok(infoPage);
    }


    //查询所有销售属性 /admin/product/baseSaleAttrList
    @GetMapping("/baseSaleAttrList")
    public Result<List<BaseSaleAttr>> baseSaleAttrList(){
        List<BaseSaleAttr> list = spuManageService.getBaseSaleAttrList();
        return Result.ok(list);
    }

    //保存spu /admin/product/saveSpuInfo
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuManageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }



}
