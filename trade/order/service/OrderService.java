package com.xyx.trade.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyx.trade.order.domain.Order;

import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 订单ID
     */
    Long createOrder(Order order);

    /**
     * 查询我的订单列表
     * 
     * @param userId   用户ID
     * @param type     订单类型（buy, sell, all）
     * @param status   订单状态
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Map<String, Object> getMyOrders(Long userId, String type, Integer status, int pageNum, int pageSize);

    /**
     * 确认收货（完成订单）
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean completeOrder(Long orderId, Long userId);

    /**
     * 根据ID查询订单
     * 
     * @param id 订单ID
     * @return 订单信息
     */
    Order getOrderById(Long id);

    /**
     * 支付订单
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean payOrder(Long orderId, Long userId);

    /**
     * 更新订单状态（管理员使用或内部流转）
     * 
     * @param orderId 订单ID
     * @param status  新状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 管理员查询所有订单列表
     */
    Map<String, Object> getAllOrders(Integer status, String keyword, int pageNum, int pageSize);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param reason  取消原因
     * @param userId  当前用户ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, String reason, Long userId);

    /**
     * 统计今日订单数
     */
    int countTodayOrder();

    /**
     * 统计今日销售额
     */
    java.math.BigDecimal sumTodayAmount();
}

