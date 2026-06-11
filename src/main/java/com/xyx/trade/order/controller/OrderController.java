package com.xyx.trade.order.controller;

import com.xyx.trade.order.domain.Order;
import com.xyx.trade.order.service.OrderService;
import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单控制器
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @ApiOperation(value = "创建订单", notes = "创建商品购买订单")
    @PostMapping("/create")
    public AjaxResult<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest requestBody,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Order order = new Order();
        order.setBuyerId(userId);
        order.setProductId(requestBody.getProductId());
        order.setBuyerRemark(requestBody.getRemark());
        order.setContactPhone(requestBody.getContactPhone());
        order.setDeliveryAddress(requestBody.getDeliveryAddress());

        Long orderId = orderService.createOrder(order);
        Order createdOrder = orderService.getOrderById(orderId);

        // 构造返回数据
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", createdOrder.getOrderNo());
        result.put("totalAmount", createdOrder.getTotalAmount());

        return AjaxResult.success("订单创建成功", result);
    }

    /**
     * 我的订单列表
     */
    @ApiOperation(value = "我的订单列表", notes = "获取当前用户的订单列表（分页）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码，默认1", dataType = "int", paramType = "query", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量，默认10", dataType = "int", paramType = "query", example = "10"),
            @ApiImplicitParam(name = "type", value = "订单类型（buy:作为买家, sell:作为卖家, all:全部）", required = true, dataType = "string", paramType = "query", example = "buy"),
            @ApiImplicitParam(name = "status", value = "订单状态筛选（0:待付款,1:待发货,2:待收货,3:已完成,4:已取消）", dataType = "int", paramType = "query")
    })
    @GetMapping("/my-orders")
    public AjaxResult<Map<String, Object>> getMyOrders(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String type,
            Integer status) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = orderService.getMyOrders(userId, type, status, pageNum, pageSize);
        return AjaxResult.success(result);
    }

    /**
     * 确认收货（完成订单）
     */
    @ApiOperation(value = "确认收货（完成订单）", notes = "买家确认收货，完成订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单ID", required = true, dataType = "long", paramType = "path", example = "1")
    })
    @PutMapping("/complete/{id}")
    public AjaxResult<String> completeOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        boolean success = orderService.completeOrder(id, userId);
        if (success) {
            return AjaxResult.success("订单已完成");
        } else {
            return AjaxResult.error("操作失败");
        }
    }

    /**
     * 支付订单
     */
    @ApiOperation(value = "支付订单", notes = "模拟订单支付流程")
    @PostMapping("/pay/{id}")
    public AjaxResult<String> payOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = orderService.payOrder(id, userId);
        if (success) {
            return AjaxResult.success("支付成功");
        } else {
            return AjaxResult.error("支付失败");
        }
    }

    /**
     * 取消订单
     */
    @ApiOperation(value = "取消订单", notes = "买家取消订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单ID", required = true, dataType = "long", paramType = "path", example = "1"),
            @ApiImplicitParam(name = "reason", value = "取消原因", dataType = "string", paramType = "query", example = "不想买了")
    })
    @PostMapping("/cancel/{id}")
    public AjaxResult<String> cancelOrder(@PathVariable Long id, String reason, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = orderService.cancelOrder(id, reason, userId);
        if (success) {
            return AjaxResult.success("订单已取消");
        } else {
            return AjaxResult.error("操作失败");
        }
    }

    /**
     * 创建订单请求对象
     */
    @ApiModel(description = "创建订单请求对象")
    public static class CreateOrderRequest {
        @ApiModelProperty(value = "商品ID", required = true, example = "1")
        private Long productId;

        @ApiModelProperty(value = "购买数量，默认1", example = "1")
        private Integer quantity;

        @ApiModelProperty(value = "买家备注", example = "请发顺丰")
        private String remark;

        @ApiModelProperty(value = "联系电话", required = true, example = "13800138000")
        private String contactPhone;

        @ApiModelProperty(value = "配送地址", example = "东校区3号楼")
        private String deliveryAddress;

        // Getters and Setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
        }
    }
}

