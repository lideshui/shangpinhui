package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * 通过Feign接口，远程调用商品详情的七个接口，进行数据汇总的类，在请求URL中传入SKU的ID即可
 */

@RestController
@RequestMapping("/api/item")
//去掉警告的下划线
@SuppressWarnings("all")
public class ItemController {

    @Autowired
    ItemService itemService;




    /**
     * 提供给前端服务调用RestFul接口实现,汇总商品详情页所需7项数据
     *
     * @param skuId - **skuInfo**：当前商品SKU信息包含SKU图片列表
     *              - **categoryView**：当前商品所属的分类信息（包含三级）
     *              - **price**：当前商品最新价格
     *              - **spuPosterList**：当前商品海报图片集合
     *              - **skuAttrList**：当前商品平台属性及属性值集合--- 规格与参数
     *              - **spuSaleAttrList**：当前商品销售属性集合选中效果
     *              - **valuesSkuJson**：切换SKU转换SKU商品json字符串信息
     * @return
     */
    @GetMapping("/{skuId}")
    public Result getItemAllData(@PathVariable("skuId") Long skuId) {
        Map<String, Object> data = itemService.getItemAllData(skuId);
        return Result.ok(data);
    }
}
