package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.model.*;
import com.atguigu.gmall.product.service.SkuManageService;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/product")
public class SkuManageController {

    @Autowired
    private SkuManageService skuManageService;

    @Autowired
    private SpuImageService spuImageService;


    //根据商品SPUID查询销售属性名称以及值，创建SKU时候要用
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> list = skuManageService.getSpuSaleAttrList(spuId);
        return Result.ok(list);
    }


    //根据spuId获取spu图片集合，创建SKU时的默认封面图片和sku_image都是出该列表中选取
    @GetMapping("/spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable("spuId") Long spuId) {
        List<SpuImage> list = spuImageService.getSpuImageList(spuId);
        return Result.ok(list);
    }


    //创建sku，保存skuInfo数据
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }


    //sku分页列表
    @GetMapping("/list/{page}/{limit}")
    public Result<IPage<SkuInfo>> getSkuInfoPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestParam("category3Id") Long category3Id
    ) {
        //用SkuInfo接收查询参数，是因为她的属性有category3
        IPage<SkuInfo> iPage = skuManageService.getSkuInfoPage(page, limit, category3Id);
        return Result.ok(iPage);
    }


    //sku上架-目前先简单写一下，后期会修改
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){
        skuManageService.onSale(skuId);
        return Result.ok();
    }

    //sku下架-目前先简单写一下，后期会修改
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){
        skuManageService.cancelSale(skuId);
        return Result.ok();
    }


}
