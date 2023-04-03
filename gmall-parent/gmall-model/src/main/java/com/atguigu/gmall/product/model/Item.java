package com.atguigu.gmall.product.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Item implements Serializable {
    private Integer id;
    private String name;
    private Long price;
}
