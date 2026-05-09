package com.xyx.trade.banner.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 轮播图实体类
 */
@ApiModel(description = "轮播图信息实体")
@Data
@TableName("xyx_banner")
public class Banner {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "轮播图 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "标题", example = "开学季促销", required = true)
    private String title;

    @ApiModelProperty(value = "图片 URL", example = "http://example.com/banner.jpg", required = true)
    private String imageUrl;

    @ApiModelProperty(value = "跳转链接", example = "http://example.com/product/1")
    private String linkUrl;

    @ApiModelProperty(value = "排序值（越小越靠前）", example = "1")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态（0:禁用，1:启用）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "开始展示时间")
    private Date startTime;

    @ApiModelProperty(value = "结束展示时间")
    private Date endTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}

