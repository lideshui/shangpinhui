package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    //RestFul商品详情获取商品图片
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        //通过id获取skuInfo对象判断有无该商品信息
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        if (skuInfo!=null){
            LambdaQueryWrapper<SkuImage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SkuImage::getSkuId,skuId);
            List<SkuImage> list = skuImageService.list(queryWrapper);
            //将商品列表放到skuInfo属性值中
            skuInfo.setSkuImageList(list);
            return skuInfo;
        }
        return null;
    }

    //RestFul商品详情获取商品价格
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfo::getId,skuId);
        SkuInfo skuInfo = skuInfoService.getOne(queryWrapper);
        return skuInfo.getPrice();
    }

    //RestFul商品详情获取平台属性
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        //获取mapper层对象
        SkuAttrValueMapper skuAttrValueMapper = (SkuAttrValueMapper) skuAttrValueService.getBaseMapper();
        List<BaseAttrInfo> attrList = skuAttrValueMapper.getAttrList(skuId);
        if (!CollectionUtils.isEmpty(attrList)) {
            //老师这里用的迭代器，一样的效果
            attrList.stream().forEach(baseAttrInfo->{
                //每次循环都是集合第一个，所以使用get(0)⚠️
                baseAttrInfo.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            });
        }
        return attrList;
    }

    //根据获取SKU转换后的JSON，实现切换商品
    @Override
    public String getSkuValueIdsMap(Long spuId) {
        //声明封装所有销售属性跟SKUID对应Map
        HashMap<String, String> mapResult = new HashMap<>();
        //查询自定义SQL
        SkuSaleAttrValueMapper skuSaleAttrValueMapper = (SkuSaleAttrValueMapper) skuSaleAttrValueService.getBaseMapper();
        List<Map> list = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        if (!CollectionUtils.isEmpty(list)) {
            //遍历List获取销售属性以及SKUID
            list.stream().forEach(map -> {
                Long skuId = (Long) map.get("sku_id");
                String valueIds = (String) map.get("value_ids");
                //将查询到的结果封入数组
                mapResult.put(valueIds, skuId.toString());
            });
        }
        return JSON.toJSONString(mapResult);
    }

}
