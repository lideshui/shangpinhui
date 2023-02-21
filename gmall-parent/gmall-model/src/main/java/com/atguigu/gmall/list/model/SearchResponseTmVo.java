package com.atguigu.gmall.list.model;

import lombok.Data;

import java.io.Serializable;

// 品牌数据
@Data
public class SearchResponseTmVo implements Serializable {
    //品牌ID
    private Long tmId;
    //品牌名称
    private String tmName;
    //品牌Logo
    private String tmLogoUrl;
}

