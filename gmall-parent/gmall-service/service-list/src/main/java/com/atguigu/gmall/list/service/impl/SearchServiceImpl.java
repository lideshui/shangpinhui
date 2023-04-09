package com.atguigu.gmall.list.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.model.*;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.atguigu.gmall.product.model.BaseCategoryView;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.atguigu.gmall.product.model.SkuInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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


    /**
     * 首页的商品检索，通过搜索条件去ES索引库中查询对应的商品信息🔍🔍🔍
     * 商品检索 使用SDE通过调用ES提供JavaClientAPI构建DSL语句-请求地址,请求体参数；解析ES响应结果
     * 业务数据检索
     * 过滤项条件聚合
     *
     * @param searchParam
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        try {
            //1. 根据用户查询条件构建SearchRequest 构建DSL语句:包括查询方式、分页、高亮、排序、字段过滤、聚合⚠️⚠️⚠️
            SearchRequest searchRequest = this.builderDSL(searchParam);
            System.out.println("----------将DSQL语句打印到控制台进行测试----------");
            System.out.println("--------------------");
            System.err.println(searchRequest.source().toString());
            System.out.println("--------------------");

            //2. 执行检索,得到响应结果
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //3. 按照要求封装响应结果⚠️⚠️⚠️
            return this.parseResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("[检索商品]异常:{}", e);
        }
        //如果没返回对应的数据，返回个空对象兜底
        return new SearchResponseVo();
    }


    /**
     * 目的:构建调用ES Http接口 请求信息(请求地址,方式,路径参数,请求体参数)
     *
     * @param searchParam
     */
    private SearchRequest builderDSL(SearchParam searchParam) {
        //1.创建SearchRequest对象-封装操作索引库请求方式--请求地址 GET goods/_search
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        //2.创建SearchSourceBuilder对象-封装请求体JSON对象参数--请求参数
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //2.1 设置查询方式 .query()设置请求体参数:"query"部分
        //2.1.1 构建完整的多条件封装Query对象-BoolQueryBuilder
        BoolQueryBuilder allBoolQueryBuilder = QueryBuilders.boolQuery();

        //2.1.2 设置关键字条件设置
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            allBoolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
        }

        //2.1.3 设置 "品牌" 条件过滤 形式=品牌ID:品牌名称
        if (StringUtils.isNotBlank(searchParam.getTrademark())) {
            String[] split = searchParam.getTrademark().split(":");
            if (split != null && split.length == 2) {
                allBoolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //2.1.4 设置 "分类" 条件过滤
        if (searchParam.getCategory1Id() != null) {
            allBoolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        if (searchParam.getCategory2Id() != null) {
            allBoolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (searchParam.getCategory3Id() != null) {
            allBoolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }

        //2.1.5 设置 "平台属性" 条件过滤 形式-平台属性Id:平台属性值名称:平台属性名
        //2.1.5.1 获取所有的用户提交平台属性条件数组
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            //构建外部的bool查询对象
            BoolQueryBuilder attrBoolQueryBuilder = QueryBuilders.boolQuery();
            //2.2.5.2 遍历平台属性过滤条件 每遍历一次 构建"nested"布尔查询对象
            for (String prop : props) {
                //根据冒号，截取平台属性Id:平台属性值名称:平台属性名
                String[] split = prop.split(":");
                if (split != null && split.length == 3) {
                    //内部每循环一次还要构建一次布尔查询
                    BoolQueryBuilder attrIdAndAttrValueBoolQueryBuilder = QueryBuilders.boolQuery();

                    //将term精准匹配的属性和值放到must的bool查询对象中
                    attrIdAndAttrValueBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    attrIdAndAttrValueBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));

                    //将内部构建完成的bool查询对象放到nest查询对象中
                    NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", attrIdAndAttrValueBoolQueryBuilder, ScoreMode.None);

                    //将nest查询对象放到外部的bool查询对象
                    attrBoolQueryBuilder.must(nestedQueryBuilder);
                }
            }

            //将外部的bool查询对象放到最外层的bool查询中
            allBoolQueryBuilder.filter(attrBoolQueryBuilder);
        }
        sourceBuilder.query(allBoolQueryBuilder);


        //2.2 设置分页 .from() .size 设置请求参数: "from","size"
        // 当前页码-1 再乘 每页显示条数
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        sourceBuilder.from(from).size(searchParam.getPageSize());


        //2.3 设置高亮 highlighter()设置请求参数中:"highlight"
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font style='color:red'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.field("title");
        sourceBuilder.highlighter(highlightBuilder);

        //2.4 设置排序 sort()设置请求参数中"sort" 形式:   排序规则:asc/desc  排序规则1:商品热度 排序规则2:价格
        if (StringUtils.isNotBlank(searchParam.getOrder())) {
            //2.4.1 获取排序条件值 按照":"进行分割，第一位是根据啥排序，第二位是升序还是降序
            String[] split = searchParam.getOrder().split(":");
            if (split != null && split.length == 2) {
                String orderField = "";
                switch (split[0]) {
                    case "1":
                        orderField = "hotScore";
                        break;
                    case "2":
                        orderField = "price";
                        break;
                }
                //2.4.2 通过排序规则判断得到排序字段
                sourceBuilder.sort(orderField, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        }

        //2.5 设置过滤字段 fetchSource() 设置请求参数中:"_source"
        sourceBuilder.fetchSource(new String[]{"id", "title", "price", "defaultImg"}, null);


        //2.6 设置聚合 aggregation() 设置请求参数中:"aggs"
        //2.6.1 设置"品牌"聚合 聚合三要素:聚合名称;聚合类型;聚合字段 terms("聚合名称").field("字段")
        //2.6.1.1 创建品牌ID聚合，单独构建聚合对象
        TermsAggregationBuilder tmIdAgg = AggregationBuilders.terms("tmIdAgg").field("tmId");//.size(30);
        //2.6.1.2 基于品牌ID聚合创建品牌名称子聚合
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"));//.size(1);
        //2.6.1.3 基于品牌ID聚合创建品牌Logo图片子聚合
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));//.size(1);
        //将聚合对象放到最外层的bool查询中
        sourceBuilder.aggregation(tmIdAgg);

        //2.6.2 设置"平台属性"聚合
        //2.6.2.1 创建"nested"聚合对象-对平台属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attrsAgg", "attrs");
        //2.6.2.2 创建平台属性ID子聚合对象，单独构建聚合对象
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        //2.6.2.2.1 基于平台属性ID聚合创建 平台属性名称聚合
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));
        //2.6.2.2.2 基于平台属性ID聚合创建 平台属性值聚合
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        //将平台属性ID聚合加入到平属性聚合中
        attrAgg.subAggregation(attrIdAgg);
        //将聚合对象放到最外层的bool查询中
        sourceBuilder.aggregation(attrAgg);

        //3.将SearchSourceBuilder对象关联到SearchRequest
        return searchRequest.source(sourceBuilder);
    }


    /**
     * 解析ES响应结果；业务数据结果；聚合结果
     *
     * @param response
     * @param searchParam
     */
    private SearchResponseVo parseResult(SearchResponse response, SearchParam searchParam) {
        //1.创建搜索响应VO对象，因为SearchParam对象缺少分页信息等，所以用VO对象
        SearchResponseVo vo = new SearchResponseVo();

        //2.封装分页信息
        vo.setPageNo(searchParam.getPageNo());
        Integer pageSize = searchParam.getPageSize();
        vo.setPageSize(pageSize);
        //2.1 获取总记录数，在hits下的total下的value有命中数据总数，即记录总数
        long total = response.getHits().getTotalHits().value;
        vo.setTotal(total);
        //2.2 计算总页数 总数%页大小能整除=总数/页大小  反之+1
        Long totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        vo.setTotalPages(totalPage);

        //3.封装检索到商品数据-注意处理高亮字段
        SearchHit[] hits = response.getHits().getHits();
        List<Goods> goodsList = new ArrayList<>();
        //长度大于0说明有命中数据
        if (hits != null && hits.length > 0) {
            //循环几次则取到几个Goods数据
            for (SearchHit hit : hits) {
                //3.1 将得到商品JSON字符串转为Java对象
                Goods goods = JSON.parseObject(hit.getSourceAsString(), Goods.class);
                //3.2 处理高亮，将Highlight中的数据替换到Goods对象中
                if(hit.getHighlightFields()!=null){
                    //取出高亮数据，得到的是一个数组
                    Text[] titles = hit.getHighlightFields().get("title").getFragments();
                    //如果数组长度不为空，则说明有高亮数据
                    if (titles != null && titles.length > 0) {
                        //从数组中取第0个，转换成字符串赋值
                        goods.setTitle(titles[0].toString());
                    }
                }
                //存储goos对象，放到集合中
                goodsList.add(goods);
            }
        }
        //将goods集合放到vo对象中
        vo.setGoodsList(goodsList);

        //4.封装品牌聚合结果
        //从聚合的桶中拿品牌的聚合结果
        Map<String, Aggregation> allAggregationMap = response.getAggregations().asMap();
        //4.1 获取品牌ID聚合对象 通过获取品牌ID桶得到聚合品牌ID
        //从桶的数组中，根据聚合的名字取出某个桶
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) allAggregationMap.get("tmIdAgg");
        if (tmIdAgg != null) {
            //将tmIdAgg桶内的桶进行遍历
            List<SearchResponseTmVo> tmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
                SearchResponseTmVo tmVo = new SearchResponseTmVo();
                //取出聚合品牌的ID放入到vo对象中
                long tmId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                tmVo.setTmId(tmId);
                //4.2 从品牌Id桶内获取品牌名称聚合对象,遍历品牌名称桶得到桶中品牌名称-只有一个
                ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
                if (tmNameAgg != null) {
                    //有且只有一个，存储的品牌的名称
                    String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                    tmVo.setTmName(tmName);
                }
                //4.3 从品牌Id桶内获取品牌图片聚合对象,遍历品牌图片桶得到桶中图片Logo-只有一个
                ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
                if (tmLogoUrlAgg != null) {
                    //有且只有一个，存储的品牌的默认图片URL
                    String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
                    tmVo.setTmLogoUrl(tmLogoUrl);
                }
                return tmVo;
                //收集一下，得到所有的vo list
            }).collect(Collectors.toList());

            //为响应对象封装聚合品牌列表
            vo.setTrademarkList(tmVoList);
        }

        //5.封装平台属性聚合结果
        //5.1 获取平台属性聚合对象，这里是嵌套类型，要使用ParsedNested
        ParsedNested attrsAgg = (ParsedNested) allAggregationMap.get("attrsAgg");
        //5.2 通过平台数据聚合对象获取平台属性ID的聚合对象,获取平台属性ID聚合桶集合
        if (attrsAgg != null) {
            ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
            if (attrIdAgg != null) {

                //5.3 遍历ID桶集合 获取平台属性ID 以及平台属性名称跟属性值
                List<SearchResponseAttrVo> attrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
                    SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
                    //获取平台属性Id
                    long atrrId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                    attrVo.setAttrId(atrrId);
                    //5.3.1 基于平台属性ID聚合对象 获取平台属性名称子聚合对象.获取平台名称桶内平台属性名称 只有一个
                    ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                    if (attrNameAgg != null) {
                        //有且只有一个，存储的平台属性的名称
                        String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                        attrVo.setAttrName(attrName);
                    }
                    //5.3.2 基于平台属性ID聚合对象 获取平台属性值子聚合对象.获取平台属性值桶内平台属性值名称
                    ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                    if (attrValueAgg != null) {
                        //遍历平台属性值桶,得到桶内每个平台属性值，因为一个平台属性可能有多个值，所以要遍历取值
                        List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket -> {
                            return ((Terms.Bucket) attrValueBucket).getKeyAsString();
                            //将属性值收集到List集合中
                        }).collect(Collectors.toList());
                        //为Vo对象赋值
                        attrVo.setAttrValueList(attrValueList);
                    }
                    return attrVo;
                    //收集Vo对象list集合
                }).collect(Collectors.toList());
                //给响应VO对象赋值:平台属性集合
                vo.setAttrsList(attrVoList);
            }
        }
        return vo;
    }

}