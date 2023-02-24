package com.atguigu.gmall.product.service;

import com.atguigu.gmall.product.model.SpuImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品图片表 业务接口类
 * @author atguigu
 * @since 2023-02-23
 */
public interface SpuImageService extends IService<SpuImage> {


    //根据spuId获取图片
    List<SpuImage> getSpuImageList(Long spuId);
}
