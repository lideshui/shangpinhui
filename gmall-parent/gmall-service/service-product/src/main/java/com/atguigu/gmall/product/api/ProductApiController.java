package com.atguigu.gmall.product.api;


import com.atguigu.gmall.product.model.*;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import com.atguigu.gmall.product.service.SkuManageService;
import com.atguigu.gmall.product.service.SpuManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

//商品模块所有的restful接口，包含/api请求地址都是微服务间内部接口调用，放到api包下用来区分
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private SkuManageService skuManageService;

    //根据三级分类创建的视图对应的Service对象
    @Autowired
    private BaseCategoryViewService baseCategoryViewService;

    @Autowired
    private SpuManageService spuManageService;


    //1。根据SkuID查询SKU商品信息包含图片列表
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfoAndImages(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = skuManageService.getSkuInfoAndImages(skuId);
        return skuInfo;
    }

    //2。根据商品SKU三级分类ID查询分类信息
    @GetMapping("/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {
        return baseCategoryViewService.getById(category3Id);
    }

    //3。根据SKUID查询商品最新价格-下单时必需实时查询，不可从缓存中获取⚠️⚠️⚠️
    @GetMapping("/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId){
        return skuManageService.getSkuPrice(skuId);
    }

    //4。根据spuId 获取海报数据
    @GetMapping("inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> getSpuPosterBySpuId(@PathVariable Long spuId){
        return spuManageService.getSpuPosterBySpuId(spuId);
    }

    //5。根据SkuID查询当前商品包含平台属性以及属性值
    @GetMapping("/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId){
        return skuManageService.getAttrList(skuId);
    }

    //6。查询当前商品所有的销售属性,判断为当前SKU拥有销售属性增加选中效果
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId){
        return spuManageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    //7。获取每一组销售属性对应SkuID组合，来完成商品页切换，超级难SQL⚠️ {"3736|3738":"24","3736|3739":"25",}
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public String getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        return skuManageService.getSkuValueIdsMap(spuId);
    }


}