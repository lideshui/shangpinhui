package com.atguigu.gmall.item.service;


import java.util.Map;

public interface ItemService {
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
    Map<String, Object> getItemAllData(Long skuId);
}