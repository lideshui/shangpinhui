package com.atguigu.gmall.list.model;

import lombok.Data;

/**
 * 接收前端参数：
 * category3Id=61&trademark=1:小米&props=23:8G:运行内存&props=24:128G:机身内存&order=1:desc
 */
@Data
public class SearchParam {

    //category3Id=61
    private Long category1Id;;//三级分类id
    private Long category2Id;
    private Long category3Id;

    // trademark=2:华为
    private String trademark;//品牌

    private String keyword;//检索的关键字

    // 排序规则  order=排序规则:升序/降序
    // 1:hotScore 2:price
    private String order = ""; // 1：综合排序/热度  2：价格

    // props=23:4G:运行内存
    //平台属性Id:平台属性值名称:平台属性名
    private String[] props;//页面提交的数组

    private Integer pageNo = 1;//分页信息
    private Integer pageSize = 3; // 每页默认显示的条数


}
