package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.model.*;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service
public class SkuManageServiceImpl implements SkuManageService {

    //注入SpuSaleAttr的持久层查询销售属性集合
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Autowired
    SkuImageService skuImageService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;


    //根据spuId 查询销售属性集合
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        List<SpuSaleAttr> list = spuSaleAttrMapper.getSpuSaleAttrList(spuId);
        return list;
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //1。添加skuInfo
        skuInfoService.save(skuInfo);

        //2。添加Sku图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.stream().forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
            });
            skuImageService.saveBatch(skuImageList);
        }

        //3。添加Sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.stream().forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
            });
            skuAttrValueService.saveBatch(skuAttrValueList);
        }

        //4。添加sku销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.stream().forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            });
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);
        }
    }

    //sku分页
    @Override
    public IPage<SkuInfo> getSkuInfoPage(Long page, Long limit, Long category3Id) {
        //创建建分页对象，一定要注意是new的page
        IPage<SkuInfo> iPage = new Page<>(page, limit);

        //2.查询分页数据 alt+enter 快速修正错误
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        //没查询条件就不用查询了
        if (category3Id != null) {
            queryWrapper.eq(SkuInfo::getCategory3Id, category3Id);
        }
        //根据最新更新日期排序
        queryWrapper.orderByDesc(SkuInfo::getUpdateTime);
        return skuInfoService.page(iPage, queryWrapper);
    }

    //上架
    @Override
    public void onSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SkuInfo::getId, skuId);
        updateWrapper.set(SkuInfo::getIsSale, 1);
        skuInfoService.update(updateWrapper);
    }

    //下架
    @Override
    public void cancelSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SkuInfo::getId, skuId);
        updateWrapper.set(SkuInfo::getIsSale, 0);
        skuInfoService.update(updateWrapper);
    }

}
