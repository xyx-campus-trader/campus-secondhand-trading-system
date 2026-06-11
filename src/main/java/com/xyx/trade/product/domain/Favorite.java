package com.xyx.trade.product.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 收藏实体类
 */
@ApiModel(description = "收藏信息实体")
@Data
@TableName("xyx_favorite")
public class Favorite {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "收藏 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户 ID", example = "1", required = true)
    private Long userId;

    @ApiModelProperty(value = "商品 ID", example = "2", required = true)
    private Long productId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "用户昵称")
    @TableField(exist = false)
    private String userNickname;

    @ApiModelProperty(value = "商品标题")
    @TableField(exist = false)
    private String productTitle;

    @ApiModelProperty(value = "商品价格")
    @TableField(exist = false)
    private java.math.BigDecimal productPrice;
}

