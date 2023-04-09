package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.model.*;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.atguigu.gmall.product.model.BaseCategoryView;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.atguigu.gmall.product.model.SkuInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ES goods索引Service类
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 常量：索引库名称
     */
    private static final String INDEX_NAME = "goods";


    /**
     * 商品文档对象录入索引-操作ES索引库🔍🔍🔍
     * 1.远程调用商品微服务获取商品相关信息
     * 2.封装索引库商品文档Goods
     * 3.调用ES将文档对象存入索引库
     *
     * @param skuId
     */
    @Override
    public void upperGoods(Long skuId) {
        try {
            //1.创建索引库文档对象：Goods
            Goods goods = new Goods();

            //2.封装商品文档对象Goods中的属性赋值⚠️⚠️⚠️
            //2.1 根据SkuID远程查询SkuInfo商品信息
            CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                //获取商品详情和图片
                SkuInfo skuInfo = productFeignClient.getSkuInfoAndImages(skuId);
                if (skuInfo != null) {
                    //为索引库文档对象赋值
                    goods.setId(skuInfo.getId());
                    goods.setTitle(skuInfo.getSkuName());
                    goods.setPrice(skuInfo.getPrice().doubleValue());
                    goods.setDefaultImg(skuInfo.getSkuDefaultImg());
                    goods.setCreateTime(new Date());
                    goods.setCreatedDate(skuInfo.getCreateTime());
                }
                return skuInfo;
            });

            //2.2 根据skuInfo的品牌ID查询品牌信息
            CompletableFuture<Void> baseCategoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
                BaseTrademark trademark = productFeignClient.getTrademarkById(skuInfo.getTmId());
                if (trademark != null) {
                    goods.setTmId(trademark.getId());
                    goods.setTmName(trademark.getTmName());
                    goods.setTmLogoUrl(trademark.getLogoUrl());
                }
            });

            //2.3 根据分类ID查询分类信息
            CompletableFuture<Void> trademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
                BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                if (categoryView != null) {
                    goods.setCategory1Id(categoryView.getCategory1Id());
                    goods.setCategory1Name(categoryView.getCategory1Name());

                    goods.setCategory2Id(categoryView.getCategory2Id());
                    goods.setCategory2Name(categoryView.getCategory2Name());

                    goods.setCategory3Id(categoryView.getCategory3Id());
                    goods.setCategory3Name(categoryView.getCategory3Name());
                }
            });

            //2.4 根据skuID查询平台属性以及值
            CompletableFuture<Void> atrrCompletableFuture = CompletableFuture.runAsync(() -> {
                List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
                if (!CollectionUtils.isEmpty(attrList)) {

                    //将baseAttrInfo的集合转换成SearchAttr的集合，因为goods文档对象存储的是SearchAttr⚠️
                    List<SearchAttr> attrs = attrList.stream().map(baseAttrInfo -> {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(baseAttrInfo.getId());
                        searchAttr.setAttrName(baseAttrInfo.getAttrName());
                        searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                        return searchAttr;
                    }).collect(Collectors.toList());
                    goods.setAttrs(attrs);
                }
            });


            //2.5 并行化执行
            CompletableFuture.allOf(
                    skuInfoCompletableFuture,
                    baseCategoryViewCompletableFuture,
                    atrrCompletableFuture
            ).join();


            //3.将索引库文档对象存入索引库ES
            //3.1 创建IndexRequest对象 封装索引库名称 当前文档ID(必须是String) 请求JSON
            IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                    .id(skuId.toString())
                    .source(JSON.toJSONString(goods), XContentType.JSON);
            //3.2 注入导入ES依赖后自动装配的客户端对象，执行同步保存 第二个参数为默认的HTTP请求头
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
            log.error("商品上架失败：{}", e);
        }
    }


    /**
     * 商品文档对象从索引库中删除-操作ES索引库🔍🔍🔍
     * 1.构建一个删除请求对象
     * 2.调用方法删除
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        try {
            //输入索引名和文档ID，构建一个删除的请求对象
            DeleteRequest request = new DeleteRequest(
                    INDEX_NAME,
                    skuId.toString());
            //调用删除方法，第二个参数为默认的HTTP请求头
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("商品下架失败：{}", e);
        }
    }


    /**
     * 使用 Redis + ZSet 实现更新商品的热度排名🍀🍀🍀
     * 提供给service-item服务调用，用户访问该sku时分值+1
     * 每次调用都判断是否为十的倍数，满足条件才更新goods索引库🔍🔍🔍
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        try {
            //1 根据skuID获取缓存中商品热度分值，对结果进行自增+1
            //1.1 构建ZSet排名Key
            String hotKey = "hotScore";
            //1.2 调用自增分值+1方法为查询商品增加分值，注意这里的ID需要转成String
            Double score = redisTemplate.opsForZSet().incrementScore(hotKey, skuId.toString(), 1);

            //2。根据skuID更新ES索引库中的排名分值，怕影响效率，所以每次上升到10的倍数时才进行更新
            if (score % 10 == 0) {
                //2.1 根据索引库主键ID查询商品文档
                GetRequest getRequest = new GetRequest(INDEX_NAME, skuId.toString());
                GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
                //取出查询到的数据，因为数据都存储在_source中
                String sourceAsString = response.getSourceAsString();
                //将JSON转换为Object
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                if (goods != null) {
                    //重新修改排名分数
                    goods.setHotScore(score.longValue());

                    //2.2 修改索引库文档，注意别导错包⚠️
                    UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, skuId.toString());
                    //将goods对象转换为Json构建修改es对象
                    updateRequest.doc(JSON.toJSONString(goods), XContentType.JSON);
                    //执行修改操作
                    restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("[更新文档热度分值失败:{}]", e);
        }

    }

}