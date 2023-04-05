package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.SpuImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品图片表 业务接口类
 */
public interface SpuImageService extends IService<SpuImage> {

    //根据spuId获取spu图片集合，创建SKU时的默认封面图片和sku_image都是出该列表中选取
    List<SpuImage> getSpuImageList(Long spuId);
}
