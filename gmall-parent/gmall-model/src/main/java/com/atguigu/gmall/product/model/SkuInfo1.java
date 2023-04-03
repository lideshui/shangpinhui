package com.atguigu.gmall.product.model;

import com.atguigu.gmall.base.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * SkuInfo
 * </p>
 *
 */
@Data
public class SkuInfo1 {

	private static final long serialVersionUID = 1L;

	private Long spuId;

	private BigDecimal price;

	private String skuName;

	private String skuDesc;

	private String weight;

	private Long tmId;

	private Long category3Id;

	private String skuDefaultImg;

	private Integer isSale;

}

