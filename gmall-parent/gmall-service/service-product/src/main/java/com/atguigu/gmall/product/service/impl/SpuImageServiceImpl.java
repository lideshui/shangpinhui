package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.model.SpuImage;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.service.SpuImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品图片表 业务实现类
 */
@Service
public class SpuImageServiceImpl extends ServiceImpl<SpuImageMapper, SpuImage> implements SpuImageService {

    //根据spuId获取spu图片集合，创建SKU时的默认封面图片和sku_image都是出该列表中选取
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        LambdaQueryWrapper<SpuImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpuImage::getSpuId, spuId);
        return this.list(queryWrapper);
    }

}
