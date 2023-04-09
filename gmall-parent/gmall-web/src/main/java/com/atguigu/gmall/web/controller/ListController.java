package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.list.model.SearchAttr;
import com.atguigu.gmall.list.model.SearchParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    /**
     * 门户页面中商品检索页面渲染
     *
     * @param searchParam
     * @return
     */
    @GetMapping("/list.html")
    public String searchHtml(SearchParam searchParam, Model model) {
        //远程调用搜索微服务查询业务数据,以及过滤数据,回显业务所需数据
        //1.进行站内搜搜索渲染业务数据,过滤项数据
        Result<Map> result = listFeignClient.search(searchParam);
        model.addAllAttributes(result.getData());
        //2.为了渲染搜索页面除了以上Feign接口响应数据外 还需要四项数据
        //2.1 封装数据 key:searchParam val:用户提交的搜索条件
        model.addAttribute("searchParam", searchParam);
        //2.2 封装回显页面地址栏请求参数数据 key:urlParam val:用户提交参数完成url地址  "list.html?keyword=手机&props="
        model.addAttribute("urlParam", this.makeUrlParam(searchParam));
        //2.3 封装回显页面选中平台属性效果数据 key:propsParamList val:选中平台属性过滤条件
        model.addAttribute("propsParamList", this.makePropPramList(searchParam.getProps()));
        //2.4 封装回显排序信息数据 key:orderMap val:排序信息   包含两个属性:1type-1综合 2价格  2sort-asc/desc
        model.addAttribute("orderMap", this.makeOrderMap(searchParam.getOrder()));
        //2.5 封装回显品牌数据 key:trademarkParam val:选中品牌
        model.addAttribute("trademarkParam", this.makeTrademarkParam(searchParam.getTrademark()));
        return "/list/index";
    }

    /**
     * 制作回显的品牌信息 结果: 品牌:品牌名称  提交参数:2:华为
     * @param trademark
     * @return
     */
    private String makeTrademarkParam(String trademark) {
        if(StringUtils.isNotBlank(trademark)){
            String[] split = trademark.split(":");
            if(split!=null && split.length==2){
                return "品牌:"+split[1];
            }
        }
        return null;
    }


    /**
     * 处理用户回显平台属性选中 面包屑
     *
     * @param props
     * @return
     */
    private List<SearchAttr> makePropPramList(String[] props) {
        List<SearchAttr> searchAttrs = new ArrayList<>();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                //平台属性过滤条件 - 平台属性ID:平台属性值:平台属性名称
                if (split != null && split.length == 3) {
                    SearchAttr searchAttr = new SearchAttr();
                    searchAttr.setAttrId(Long.valueOf(split[0]));
                    searchAttr.setAttrValue(split[1]);
                    searchAttr.setAttrName(split[2]);
                    searchAttrs.add(searchAttr);
                }
            }
        }
        return searchAttrs;
    }

    /**
     * 用来制作回显浏览器地址栏中请求地址
     *
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder urlStringBuilder = new StringBuilder("/list.html?");
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            urlStringBuilder.append("&keyword=" + searchParam.getKeyword());
        }
        if (StringUtils.isNotBlank(searchParam.getTrademark())) {
            urlStringBuilder.append("&trademark=" + searchParam.getTrademark());
        }
        if (searchParam.getCategory1Id() != null) {
            urlStringBuilder.append("&category1Id=" + searchParam.getCategory1Id());
        }
        if (searchParam.getCategory2Id() != null) {
            urlStringBuilder.append("&category2Id=" + searchParam.getCategory2Id());
        }
        if (searchParam.getCategory3Id() != null) {
            urlStringBuilder.append("&category3Id=" + searchParam.getCategory3Id());
        }
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                urlStringBuilder.append("&props=" + prop);
            }
        }
        return urlStringBuilder.toString();
    }

    /**
     * 根据用户选择排序返回选中排序Map
     *
     * @param order
     * @return
     */
    private Map makeOrderMap(String order) {
        if (StringUtils.isNotBlank(order)) {
            String[] split = order.split(":");
            if (split != null && split.length == 2) {
                HashMap<String, String> orderMap = new HashMap<>();
                orderMap.put("type", split[0]);
                orderMap.put("sort", split[1]);
                return orderMap;
            }
        }else{
            //如果前端页面没有提交排序字段,需要给他设置默认排序
            HashMap<String, String> orderMap = new HashMap<>();
            orderMap.put("type", "1");
            orderMap.put("sort", "desc");
            return orderMap;
        }
        return null;
    }
}
