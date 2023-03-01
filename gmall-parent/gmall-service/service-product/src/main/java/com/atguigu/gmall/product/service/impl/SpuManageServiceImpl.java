package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.model.*;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SpuManageServiceImpl implements SpuManageService {

    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    BaseSaleAttrService baseSaleAttrService;

    @Autowired
    private SpuImageService spuImageService;

    @Autowired
    private SpuSaleAttrService spuSaleAttrService;

    @Autowired
    private SpuSaleAttrValueService spuSaleAttrValueService;

    @Autowired
    private SpuPosterService spuPosterService;


    //分页查询商品SPU列表
    @Override
    public IPage<SpuInfo> getSpuByPage(IPage<SpuInfo> infoPage, Long category3Id) {
        LambdaQueryWrapper<SpuInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (category3Id != null) {
            queryWrapper.eq(SpuInfo::getCategory3Id, category3Id);
        }
        queryWrapper.orderByDesc(SpuInfo::getUpdateTime);
        return spuInfoService.page(infoPage, queryWrapper);
    }

    //查询基础销售属性
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> list = baseSaleAttrService.list();
        return list;
    }


    // 保存商品SPU信息

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        //1.保存商品基本信息到spu_info表
        spuInfoService.save(spuInfo);

        //2.保存商品图片到spu_image表 关联到商品spu
        //获取商品图片集合
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //如果不为空，说明有图片，遍历添加
        if (!CollectionUtils.isEmpty(spuImageList)) {
            List<SpuImage> spuImages = spuImageList.stream().map(spuImage -> {
                spuImage.setSpuId(spuInfo.getId());
                return spuImage;
            }).collect(Collectors.toList());
            spuImageService.saveBatch(spuImages);
        }


        //3.保存商品海报图片到spu_poster表 关联到商品spu
        //获取海报图片集合
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        //如果不为空，说明有图片，遍历添加
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            List<SpuPoster> spuPosters = spuPosterList.stream().map(spuPoster -> {
                spuPoster.setSpuId(spuInfo.getId());
                return spuPoster;
            }).collect(Collectors.toList());
            spuPosterService.saveBatch(spuPosters);
        }

        //4.保存商品Spu对应的销售属性名称到spu_sale_attr表中 。。
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //判断是否添加了销售属性
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            spuSaleAttrList.stream().forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrService.save(spuSaleAttr);

                //5.保存商品Spu对应销售属性值表到spu_sale_attr_value表 。。
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                //判断销售属性是否有值
                spuSaleAttrValueList.stream().forEach(spuSaleAttrValue -> {
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    //设置当前销售属性值对应属性名称
                    spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                    spuSaleAttrValueService.save(spuSaleAttrValue);
                });

            });
        }


    }

    //根据spuid获取商品海报
    @GmallCache(prefix = "spuPosterBySpuId:")
    @Override
    public List<SpuPoster> getSpuPosterBySpuId(Long spuId) {
        LambdaQueryWrapper<SpuPoster> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpuPoster::getSpuId, spuId);
        return spuPosterService.list(queryWrapper);
    }

    @GmallCache(prefix = "spuSaleAttrListCheckBySku:", suffix = ":info")
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        SpuSaleAttrMapper spuSaleAttrMapper = (SpuSaleAttrMapper) spuSaleAttrService.getBaseMapper();
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }


}
