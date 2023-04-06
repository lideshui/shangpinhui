package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.model.BaseCategoryView;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 根据三级分类创建的视图的数据表对应的Service实现类
 */
@Service
public class BaseCategoryViewServiceImpl extends ServiceImpl<BaseCategoryViewMapper,BaseCategoryView> implements BaseCategoryViewService {

    //查询所有分类列表 分类嵌套结果:一级分类分类对象中包含二级分类集合;在二级分类对象中包含三级分类集合-商城首页产品分类使用⚠️
    @Override
    public List<JSONObject> getBaseCategoryList() {
        //1.构建所有一级分类集合对象
        List<JSONObject> resultList = new ArrayList<>();

        //2.查询分类视图获取所有的分类集合
        List<BaseCategoryView> allCategoryList = this.list();

        //3.对所有分类集合进行分组:根据一级分类ID分组
        Map<Long, List<BaseCategoryView>> category1ListMap =
                allCategoryList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index = 1;
        //4.遍历分组后Map处理一级分类数据
        Set<Map.Entry<Long, List<BaseCategoryView>>> category1Entries = category1ListMap.entrySet();
        for (Map.Entry<Long, List<BaseCategoryView>> category1Entry : category1Entries) {
            JSONObject category1 = new JSONObject();
            //4.1 获取一级分类ID
            Long category1Id = category1Entry.getKey();
            //4.2 获取一级分类名称，从哪儿获取都一样的数据，这里从list集合第一个获取
            String category1Name = category1Entry.getValue().get(0).getCategory1Name();

            //按照指定的前端格式拼接到JSONObject对象，它可以像Json对象一样存储数据
            category1.put("index", index++);
            category1.put("categoryId", category1Id);
            category1.put("categoryName", category1Name);


            //5构建所有二级分类集合对象
            List<JSONObject> resultList2 = new ArrayList<>();

            //5.1将当前当前一级分类的二级分类集合对象集合聚合分组
            List<BaseCategoryView> baseCategory2List = category1Entry.getValue();
            Map<Long, List<BaseCategoryView>> category2ListMap =
                    baseCategory2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            //5.2遍历二级分类集合，存储到指定的JSONObject中
            Set<Map.Entry<Long, List<BaseCategoryView>>> category2Entries = category2ListMap.entrySet();
            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : category2Entries) {
                JSONObject category2 = new JSONObject();
                //5.3 获取二级分类ID
                Long category2Id = category2Entry.getKey();
                //5.4 获取2级分类名称，从哪儿获取都一样的数据，这里从list集合第一个获取
                String category2Name = category2Entry.getValue().get(0).getCategory2Name();

                //5.5按照指定的前端格式拼接到JSONObject对象，它可以像Json对象一样存储数据
                category2.put("categoryId", category2Id);
                category2.put("categoryName", category2Name);
                resultList2.add(category2);


                //6遍历当前二级分类的三级分类
                List<JSONObject> resultList3 = new ArrayList<>();
                List<BaseCategoryView> baseCategory3List = category2Entry.getValue();

                //6.1遍历三级分类集合
                baseCategory3List.stream().forEach(category3Info -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId", category3Info.getCategory3Id());
                    category3.put("categoryName", category3Info.getCategory3Name());
                    resultList3.add(category3);
                });

                //6.2将当前二级分类的三级分类数据添加进去
                category2.put("categoryChild", resultList3);
            }


            //7将当前一级分类的二级分类数据添加进去
            category1.put("categoryChild", resultList2);


            //8将一级分类信息添加到集合
            resultList.add(category1);

        }
        return resultList;
    }
}
