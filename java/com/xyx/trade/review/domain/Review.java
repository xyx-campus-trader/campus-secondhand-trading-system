package com.xyx.trade.review.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xyx.trade.user.domain.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

/**
 * 评价实体类
 */
@ApiModel(description = "评价信息实体")
@TableName("xyx_review")
public class Review {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "评价 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "订单 ID", example = "1", required = true)
    private Long orderId;

    @ApiModelProperty(value = "评价者 ID", example = "1", required = true)
    private Long reviewerId;

    @ApiModelProperty(value = "被评价者 ID", example = "2", required = true)
    private Long targetId;

    @ApiModelProperty(value = "类型（0:买家评价卖家，1:卖家评价买家）", example = "0")
    private Integer type;

    @ApiModelProperty(value = "评分（1-5 星）", example = "5", required = true)
    private Integer rating;

    @ApiModelProperty(value = "评价内容", example = "东西很好，值得购买")
    private String content;

    @ApiModelProperty(value = "评价标签（逗号分隔）", example = "质量好，物流快")
    private String tags;

    @ApiModelProperty(value = "是否匿名（0:否，1:是）", example = "0")
    private Integer isAnonymous;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "管理员/卖家回复")
    private String adminReply;

    @ApiModelProperty(value = "回复时间")
    private Date replyTime;

    @ApiModelProperty(value = "评价者信息")
    @TableField(exist = false)
    private User reviewer;

    @ApiModelProperty(value = "回复列表")
    @TableField(exist = false)
    private List<ReviewReply> replyList;

    @ApiModelProperty(value = "评价者昵称")
    @TableField(exist = false)
    private String reviewerNickname;

    @ApiModelProperty(value = "商品名称")
    @TableField(exist = false)
    private String productName;

    @ApiModelProperty(value = "被评价人昵称")
    @TableField(exist = false)
    private String targetNickname;

    // 无参构造方法
    public Review() {
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Integer isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public Date getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(Date replyTime) {
        this.replyTime = replyTime;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public List<ReviewReply> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<ReviewReply> replyList) {
        this.replyList = replyList;
    }

    public String getReviewerNickname() {
        return reviewerNickname;
    }

    public void setReviewerNickname(String reviewerNickname) {
        this.reviewerNickname = reviewerNickname;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTargetNickname() {
        return targetNickname;
    }

    public void setTargetNickname(String targetNickname) {
        this.targetNickname = targetNickname;
    }
}

