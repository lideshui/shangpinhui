package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.model.BaseCategoryTrademark;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.atguigu.gmall.product.model.CategoryTrademarkVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类品牌中间表
 */
@Api(tags = "分类品牌中间表控制器")
@RestController
@RequestMapping("/admin/product/baseCategoryTrademark/") //TODO 填写基础映射URL
public class BaseCategoryTrademarkController {

    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;


    //根据category3Id查询分类下关联的品牌列表，选完三个分类后出现该分类对应的品牌
    @GetMapping("/findTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findTrademarkList(@PathVariable("category3Id") Long category3Id) {
        List<BaseTrademark> list = baseCategoryTrademarkService.findTrademarkList(category3Id);
        return Result.ok(list);
    }


    //查询当前分类可选品牌列表，添加品牌时候调用
    @GetMapping("/findCurrentTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> getCurrentTrademark(@PathVariable("category3Id") Long category3Id){
        List<BaseTrademark> list = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(list);
    }


    //将品牌关联到分类，多选批量添加p品牌的接口
    @PostMapping("/save")
    //CategoryTrademarkVo是中间类⚠️，专门为了处理将品牌关联到分类的pojo类
    public Result saveBasecategoryTrademark(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        baseCategoryTrademarkService.saveBasecategoryTrademark(categoryTrademarkVo);
        return Result.ok();
    }


    //删除分类品牌关联
    @DeleteMapping("/remove/{category3Id}/{trademarkId}")
    public Result removeCategoryTrademark(@PathVariable("category3Id") Long category3Id, @PathVariable("trademarkId") Long trademarkId){
        baseCategoryTrademarkService.removeCategoryTrademark(category3Id, trademarkId);
        return Result.ok();
    }


}
