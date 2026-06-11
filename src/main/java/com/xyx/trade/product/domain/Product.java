package com.xyx.trade.product.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体类
 */
@ApiModel(description = "商品信息实体")
@TableName("xyx_product")
public class Product {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "商品 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "发布用户 ID", example = "1", required = true)
    private Long userId;

    @ApiModelProperty(value = "分类 ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "商品标题", example = "iPhone 12 二手", required = true)
    private String title;

    @ApiModelProperty(value = "商品描述", example = "95 新，无划痕")
    private String description;

    @ApiModelProperty(value = "价格", example = "3999.00", required = true)
    private BigDecimal price;

    @ApiModelProperty(value = "原价", example = "5999.00")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "封面图 URL", example = "http://example.com/cover.jpg", required = true)
    private String coverImage;

    @ApiModelProperty(value = "商品图片列表")
    private String images;

    @ApiModelProperty(value = "状态（0:下架，1:上架，2:已售出，3:已删除）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "新旧程度（0:全新，1:几乎全新，2:轻微使用，3:明显使用痕迹）", example = "1")
    @TableField(value = "`condition`")
    private Integer condition;

    @ApiModelProperty(value = "浏览数", example = "100")
    private Integer viewCount;

    @ApiModelProperty(value = "点赞数", example = "20")
    private Integer likeCount;

    @ApiModelProperty(value = "交易地点", example = "东校区食堂")
    private String location;

    @ApiModelProperty(value = "是否可议价（0:否，1:是）", example = "1")
    private Integer isNegotiable;

    @ApiModelProperty(value = "是否支持配送（0:否，1:是）", example = "0")
    private Integer isDelivery;

    @ApiModelProperty(value = "校区", example = "东校区")
    private String campus;

    @ApiModelProperty(value = "是否收藏", example = "false")
    @TableField(exist = false)
    private Boolean isFavorite;

    @ApiModelProperty(value = "卖家昵称")
    @TableField(exist = false, value = "seller_nickname")
    private String sellerNickname;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    // 无参构造方法
    public Product() {
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCondition() {
        return condition;
    }

    public void setCondition(Integer condition) {
        this.condition = condition;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getIsNegotiable() {
        return isNegotiable;
    }

    public void setIsNegotiable(Integer isNegotiable) {
        this.isNegotiable = isNegotiable;
    }

    public Integer getIsDelivery() {
        return isDelivery;
    }

    public void setIsDelivery(Integer isDelivery) {
        this.isDelivery = isDelivery;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getSellerNickname() {
        return sellerNickname;
    }

    public void setSellerNickname(String sellerNickname) {
        this.sellerNickname = sellerNickname;
    }

    public String getSellerName() {
        return sellerNickname;
    }

    public void setSellerName(String sellerName) {
        this.sellerNickname = sellerName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

