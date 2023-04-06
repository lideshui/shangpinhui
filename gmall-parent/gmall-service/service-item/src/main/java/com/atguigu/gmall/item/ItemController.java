package com.atguigu.gmall.item;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * 通过Feign接口，远程调用商品详情的七个接口，进行数据汇总的类，在请求URL中传入SKU的ID即可
 */

@RestController
@RequestMapping("/api/item")
//去掉警告的下划线
@SuppressWarnings("all")
public class ItemController {

    @Autowired
    private ProductFeignClient productFeignClient;


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
        HashMap<String, Object> data = new HashMap<>();
        //1.远程调用商品服务-根据skuID查询商品sku信息
        SkuInfo skuInfo = productFeignClient.getSkuInfoAndImages(skuId);
        if (skuInfo != null) {
            data.put("skuInfo", skuInfo);
        }
        //2.根据商品Sku三家分类ID查询分类信息
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if (categoryView != null) {
            data.put("categoryView", categoryView);
        }

        //3.根据SKuID查询价格
        //尽管SkuInfo中已经有价格了，但他会存到缓存里，是我们还是必需再查一次实时最新价格⚠️
        BigDecimal price = productFeignClient.getSkuPrice(skuId);
        if (price != null) {
            data.put("price", price);
        }

        //4.根据Sku所属的SpuID查询海报图片列表
        List<SpuPoster> spuPosterList = productFeignClient.getSpuPosterBySpuId(skuInfo.getSpuId());
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            data.put("spuPosterList", spuPosterList);
        }

        //5.根据SkuID查询商品平台属性列表
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        if (!CollectionUtils.isEmpty(attrList)) {
            data.put("skuAttrList", attrList);
        }

        //6.根据spuId,skuId查询当前商品销售属性(带选中效果)
        List<SpuSaleAttr> listCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
        if (!CollectionUtils.isEmpty(listCheckBySku)) {
            data.put("spuSaleAttrList", listCheckBySku);
        }

        //7.切换SKU转换SKU商品json字符串信息
        String valuesSkuJson = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        if(StringUtils.isNotBlank(valuesSkuJson)){
            data.put("valuesSkuJson", valuesSkuJson);
        }
        return Result.ok(data);
    }
}
