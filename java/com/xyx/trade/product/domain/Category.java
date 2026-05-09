package com.xyx.trade.product.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 商品分类实体类
 * 对应数据库表: category
 */
@ApiModel(description = "商品分类实体")
@Data
public class Category {
    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID", example = "1")
    private Long id;

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称", example = "电子产品")
    private String name;

    /**
     * 父分类ID（0表示一级分类）
     */
    @ApiModelProperty(value = "父分类ID（0表示一级分类）", example = "0")
    private Long parentId;

    /**
     * 排序值
     */
    @ApiModelProperty(value = "排序值", example = "1")
    private Integer sortOrder;

    /**
     * 状态（0:禁用,1:启用）
     */
    @ApiModelProperty(value = "状态（0:禁用,1:启用）", example = "1")
    private Integer status;

    /**
     * 分类图标
     */
    @ApiModelProperty(value = "分类图标", example = "icon-electronics")
    private String icon;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
