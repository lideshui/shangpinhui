package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import com.atguigu.gmall.product.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

//必须和RestFul接口一致，因为是根据其信息创建代理对象调用方法

//fallback注解是服务降级类
//value写再nacos注册的服务名
@FeignClient(value = "service-product", fallback = ProductDegradeFeignClient.class)   //baseUrl:http://service-product
public interface ProductFeignClient {


    //根据SkuID查询sku信息以及图片
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")  // http://service-product//api/product/inner/getSkuInfo/{skuId}
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);


    //根据商品SKU三级分类ID查询分类信息
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);


    //根据SKUID查询商品最新价格
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);


    //根据SPUID查询详情页海报图片列表
    @GetMapping("/api/product/inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> getSpuPosterBySpuId(@PathVariable("spuId") Long spuId);


    //根据SkuID查询当前商品包含平台属性以及属性值
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId);

    //查询当前商品所有的销售属性,判断为当前SKU拥有销售属性增加选中效果
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId);


    //获取每一组销售属性对应SkuID组合，来完成商品页切换，超级难SQL⚠️ {"3736|3738":"24","3736|3739":"25",}
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    public String getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    //查询所有分类列表 分类嵌套结果:一级分类分类对象中包含二级分类集合;在二级分类对象中包含三级分类集合
    @GetMapping("/api/product/inner/getBaseCategoryList")
    public List<JSONObject> getBaseCategoryList();

    //根据品牌ID查询品牌信息
    @GetMapping("/api/product/inner/getTrademark/{tmId}")
    public BaseTrademark getTrademarkById(@PathVariable("tmId") Long tmId);


}