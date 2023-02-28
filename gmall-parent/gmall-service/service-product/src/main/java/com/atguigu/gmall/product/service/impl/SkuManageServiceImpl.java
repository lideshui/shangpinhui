package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.model.*;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


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
//    @Override
//    public SkuInfo getSkuInfo(Long skuId) {
//        //通过id获取skuInfo对象判断有无该商品信息
//        SkuInfo skuInfo = skuInfoService.getById(skuId);
//        if (skuInfo!=null){
//            LambdaQueryWrapper<SkuImage> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(SkuImage::getSkuId,skuId);
//            List<SkuImage> list = skuImageService.list(queryWrapper);
//            //将商品列表放到skuInfo属性值中
//            skuInfo.setSkuImageList(list);
//            return skuInfo;
//        }
//        return null;
//    }

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




    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;




    /**
     * 查询商品信息优化 采用Redisson实现分布式锁
     * 1.优先从缓存中获取数据
     * 2.获取分布式锁
     * 3.执行查询数据库逻辑
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        try {
            //1.优先从分布式缓存Redis 中获取数据
            //1.1 构建商品缓存业务Key 形式: sku:商品ID:info
            String dataKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //1.2 从Redis分布式缓存中获取数据  有值:直接返回 没有:获取分布式锁后执行业务
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(dataKey);
            if (skuInfo == null) {
                //2.尝试获取分布锁-采用Redisson实现分布式锁
                ///2.1 构建商品锁名称 形式:sku:商品id:lock
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                try {
                    //2.2 创建锁对象,尝试获取锁
                    boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                    if (flag) {
                        //2.3 执行查询数据库业务
                        //2.3.1 查询商品信息
                        skuInfo = this.getSkuInfoAndImagesFromDB(skuId);
                        if (skuInfo == null) {
                            //将null对象暂存到redis
                            redisTemplate.opsForValue().set(dataKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.MILLISECONDS);
                            return skuInfo;
                        } else {
                            //2.3.2 将商品信息放入缓存
                            redisTemplate.opsForValue().set(dataKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            //2.3.3 返回结果
                            return skuInfo;
                        }
                    } else {
                        //获取锁失败,自旋
                        return this.getSkuInfo(skuId);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //3.业务执行完毕,将锁释放掉
                    lock.unlock();
                }

            } else {
                //命中缓存数据,直接返回
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //兜底操作:查询数据库
        return this.getSkuInfoAndImagesFromDB(skuId);
    }


    /**
     * 根据SkuID查询sku信息以及图片
     *
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoAndImagesFromDB(Long skuId) {
        //1.根据主键ID查询商品Sku信息
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        //2.根据SKuID查询商品SKU的图片列表
        if (skuInfo != null) {
            LambdaQueryWrapper<SkuImage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SkuImage::getSkuId, skuId);
            List<SkuImage> skuImageList = skuImageService.list(queryWrapper);
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

}
