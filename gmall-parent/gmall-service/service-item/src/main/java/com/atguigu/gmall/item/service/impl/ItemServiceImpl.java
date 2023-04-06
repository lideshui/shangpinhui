package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.*;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedissonClient redissonClient;


    /**
     * 汇总商品详情页所需数据
     *
     * @param skuId - **skuInfo**：当前商品SKU信息包含SKU图片列表
     *              - **categoryView**：当前商品所属的分类信息（包含三级）
     *              - **price**：当前商品最新价格
     *              - **spuPosterList**：当前商品海报图片集合
     *              - **skuAttrList**：当前商品平台属性及属性值集合--- 规格与参数
     *              - **spuSaleAttrList**：当前商品销售属性集合选中效果
     *              - **valuesSkuJson**：切换SKU转换SKU商品json字符串信息
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItemAllData(Long skuId) {
        //创建存储响应结果的Map数组
        HashMap<String, Object> data = new HashMap<>();

        //0.判断用户要查询的商品是否不存在,如果不存在直接返回null，开发阶段为了方便测试可以暂时注释，测试阶段再放开🍀🍀🍀
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)) {
            return data;
        }

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
        return data;
    }
}
