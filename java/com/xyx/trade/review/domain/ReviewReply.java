package com.xyx.trade.review.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 评价回复实体类
 */
@ApiModel(description = "评价回复信息实体")
@Data
public class ReviewReply {
    @ApiModelProperty(value = "回复 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "关联评价 ID", example = "1", required = true)
    private Long reviewId;

    @ApiModelProperty(value = "回复人 ID", example = "1", required = true)
    private Long userId;

    @ApiModelProperty(value = "回复内容", example = "感谢您的评价")
    private String content;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}

