package com.xyx.trade.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xyx.trade.product.domain.Product;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 */
@ApiModel(description = "订单信息实体")
@TableName("xyx_order")
public class Order {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "订单 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "订单编号", example = "20260402150000123456")
    private String orderNo;

    @ApiModelProperty(value = "买家 ID", example = "1", required = true)
    private Long buyerId;

    @ApiModelProperty(value = "卖家 ID", example = "2", required = true)
    private Long sellerId;

    @ApiModelProperty(value = "商品 ID", example = "1", required = true)
    private Long productId;

    @ApiModelProperty(value = "订单总金额", example = "3999.00", required = true)
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "状态（0:待付款，1:待发货，2:待收货，3:已完成，4:已取消，5:退款中）", example = "0")
    private Integer status;

    @ApiModelProperty(value = "支付方式（ALIPAY,WECHAT,CASH）", example = "ALIPAY")
    private String paymentMethod;

    @ApiModelProperty(value = "支付时间")
    private Date paymentTime;

    @ApiModelProperty(value = "发货时间")
    private Date deliveryTime;

    @ApiModelProperty(value = "完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "取消时间")
    private Date cancelTime;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "买家备注")
    private String buyerRemark;

    @ApiModelProperty(value = "卖家备注")
    private String sellerRemark;

    @ApiModelProperty(value = "配送地址")
    private String deliveryAddress;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "商品信息")
    @TableField(exist = false)
    private Product product;

    @ApiModelProperty(value = "商品名称")
    @TableField(exist = false, value = "product_name")
    private String productName;

    @ApiModelProperty(value = "买家昵称")
    @TableField(exist = false, value = "buyer_name")
    private String buyerName;

    @ApiModelProperty(value = "卖家昵称")
    @TableField(exist = false, value = "seller_name")
    private String sellerName;

    @ApiModelProperty(value = "买家昵称")
    @TableField(exist = false)
    private String buyerNickname;

    @ApiModelProperty(value = "卖家昵称")
    @TableField(exist = false)
    private String sellerNickname;

    @ApiModelProperty(value = "商品标题")
    @TableField(exist = false)
    private String productTitle;

    @ApiModelProperty(value = "商品价格")
    @TableField(exist = false)
    private BigDecimal price;

    // 无参构造方法
    public Order() {
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime) {
        this.paymentTime = paymentTime;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Date getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getBuyerRemark() {
        return buyerRemark;
    }

    public void setBuyerRemark(String buyerRemark) {
        this.buyerRemark = buyerRemark;
    }

    public String getSellerRemark() {
        return sellerRemark;
    }

    public void setSellerRemark(String sellerRemark) {
        this.sellerRemark = sellerRemark;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getBuyerNickname() {
        return buyerNickname;
    }

    public void setBuyerNickname(String buyerNickname) {
        this.buyerNickname = buyerNickname;
    }

    public String getSellerNickname() {
        return sellerNickname;
    }

    public void setSellerNickname(String sellerNickname) {
        this.sellerNickname = sellerNickname;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

