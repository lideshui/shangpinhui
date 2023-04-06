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


    //根据spuId 查询销售属性集合，创建SKU时候要用
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        List<SpuSaleAttr> list = spuSaleAttrMapper.getSpuSaleAttrList(spuId);
        return list;
    }


    /**
     * 保存SKU信息
     * 1.将SKU基本信息存入sku_info表中
     * 2.将提交SKU图片存入sku_image表 关联SKU  设置sku_id逻辑外键
     * 3.将提交的平台属性列表 批量保存 sku_attr_value  关联SKU  设置sku_id逻辑外键
     * 4.将提交的销售属性列表 批量保存 sku_sale_attr_value  关联SKU  设置sku_id逻辑外键
     *
     * @param skuInfo SKU相关信息
     */
    //创建商品SKU信息，创建时要对sku属性、图片、销售属性、销售属性值进行赋值
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


    //sku上架-目前先简单写一下，后期会修改
    @Override
    public void onSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SkuInfo::getId, skuId);
        updateWrapper.set(SkuInfo::getIsSale, 1);
        skuInfoService.update(updateWrapper);
    }


    //sku下架-目前先简单写一下，后期会修改
    @Override
    public void cancelSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SkuInfo::getId, skuId);
        updateWrapper.set(SkuInfo::getIsSale, 0);
        skuInfoService.update(updateWrapper);
    }


    //根据SkuID查询SKU商品信息包含图片列表-product微服务远程调用接口⚠️
    @Override
    public SkuInfo getSkuInfoAndImages(Long skuId) {
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


    //根据商品SKU三级分类ID查询分类信息-product微服务远程调用接口⚠️
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfo::getId,skuId);
        SkuInfo skuInfo = skuInfoService.getOne(queryWrapper);
        return skuInfo.getPrice();
    }


    //根据SkuID查询当前商品包含平台属性以及属性值-product微服务远程调用接口⚠️
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        //获取mapper层对象
        SkuAttrValueMapper skuAttrValueMapper = (SkuAttrValueMapper) skuAttrValueService.getBaseMapper();
        //调用skuAttrValueMapper，自定义SQL查询语句，获取平台属性值⚠️
        List<BaseAttrInfo> attrList = skuAttrValueMapper.getAttrList(skuId);
        if (!CollectionUtils.isEmpty(attrList)) {
            //老师这里用的迭代器，一样的效果
            attrList.stream().forEach(baseAttrInfo->{
                //要去BaseAttrInfo类中添加attrValue属性⚠️⚠️⚠️
                //每次循环都是集合第一个，所以使用get(0)⚠️
                baseAttrInfo.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            });
        }
        return attrList;
    }


    //获取每一组销售属性对应SkuID组合，来完成商品页切换-product微服务远程调用接口⚠️
    @Override
    public String getSkuValueIdsMap(Long spuId) {
        //声明封装所有销售属性跟SKUID对应Map
        HashMap<String, String> mapResult = new HashMap<>();
        //查询自定义SQL
        SkuSaleAttrValueMapper skuSaleAttrValueMapper = (SkuSaleAttrValueMapper) skuSaleAttrValueService.getBaseMapper();
        //调用skuSaleAttrValueMapper，自定义SQL语句，获得{"3736|3738":"24","3736|3739":"25",}格式的数据实现商品切换⚠️
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
