package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.service.BaseCategory1Service;
import com.atguigu.gmall.product.service.BaseCategory2Service;
import com.atguigu.gmall.product.service.BaseCategory3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: TODD
 * @AllClassName: com.atguigu.gmall.product.controller.BaseCategoryApiController
 */
//需要返回JSON，所以需要RestBody +Controller
@RestController
@RequestMapping("/admin/product")
public class BaseCategoryApiController {

    //注入业务层对象
    @Autowired
    private BaseCategory1Service baseCategory1Service;

    @Autowired
    private BaseCategory2Service baseCategory2Service;

    @Autowired
    private BaseCategory3Service baseCategory3Service;

    @Autowired
    private BaseAttrInfoService baseAttrInfoService;

    //查询一级分类列表
    @GetMapping("/getGategory1")
    public Result getCategory1(){
        List<BaseCategory1> list = baseCategory1Service.getCategory1();
        return Result.ok(list);
    }

    //根据一级分类ID查询二级分类
    @GetMapping("/getGategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id){
        List<BaseCategory2> list = baseCategory2Service.getCategory2(category1Id);
        return Result.ok(list);
    }

    //根据二级分类id查询三级分类
    @GetMapping("/getGategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id){
        List<BaseCategory3> list = baseCategory3Service.getCategory3(category2Id);
        return Result.ok(list);
    }

    //根据1\2\3级分类id获取平台属性
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getAttrInfoList (
            @PathVariable("category1Id") Long category1Id,
            @PathVariable("category2Id") Long category2Id,
            @PathVariable("category3Id") Long category3Id){
        List<BaseAttrInfo> list = baseAttrInfoService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(list);
    }

}
