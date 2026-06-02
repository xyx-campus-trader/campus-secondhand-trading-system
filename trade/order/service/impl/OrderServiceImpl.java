package com.xyx.trade.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.order.domain.Order;
import com.xyx.trade.order.mapper.OrderMapper;
import com.xyx.trade.order.service.OrderService;
import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.mapper.ProductMapper;
import com.xyx.trade.user.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 创建订单流程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Order order) {
        // 1. 验证商品（使用悲观锁防止超卖）
        Product product = productMapper.selectByIdForUpdate(order.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (product.getStatus() != 1) { // 1:上架
            throw new ServiceException("商品已下架或已售出");
        }
        // 校验：买家不能是卖家自己
        if (product.getUserId().equals(order.getBuyerId())) {
            throw new ServiceException("不能购买自己发布的商品");
        }

        // 2. 填充订单关键信息
        order.setSellerId(product.getUserId());
        order.setTotalAmount(product.getPrice());
        order.setStatus(0); // 初始状态为 0:待付款
        order.setOrderNo(generateOrderNo()); // 调用工具方法生成唯一单号

        // 3. 保存订单（使用 MP 的 insert）
        baseMapper.insert(order);

        // 4. 锁定商品为已售出状态，防止其他用户重复下单
        int locked = productMapper.updateStatusCAS(order.getProductId(), 1, 2);
        if (locked == 0) {
            throw new ServiceException("商品已被其他用户下单");
        }

        return order.getId();
    }

    /**
     * 分页查询当前用户的订单列表
     * 
     * @param userId   当前登录用户ID
     * @param type     查询类型：buy(我买的), sell(我卖的), all(全部)
     * @param status   可选的状态筛选
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 包含列表及总条数的分页结果集
     */
    @Override
    public Map<String, Object> getMyOrders(Long userId, String type, Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;

        // 根据 type 参数决定查询「我买的」还是「我卖的」
        List<Order> list;
        int total;
        
        if ("buy".equals(type)) {
            // 我买的订单：只查 buyer_id = userId 的订单
            list = baseMapper.selectMyOrders(userId, userId, "buy", status, offset, pageSize);
            total = baseMapper.selectMyOrdersCount(userId, userId, "buy", status);
        } else if ("sell".equals(type)) {
            // 我卖的订单：只查 seller_id = userId 的订单
            list = baseMapper.selectMyOrders(userId, userId, "sell", status, offset, pageSize);
            total = baseMapper.selectMyOrdersCount(userId, userId, "sell", status);
        } else {
            // 全部订单：查 buyer_id = userId 或 seller_id = userId 的订单
            list = baseMapper.selectMyOrders(userId, userId, "all", status, offset, pageSize);
            total = baseMapper.selectMyOrdersCount(userId, userId, "all", status);
        }

        // 填充商品信息
        for (Order order : list) {
            Product product = productMapper.selectById(order.getProductId());
            order.setProduct(product);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 确认收货（完成订单）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId, Long userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }

        // CAS 更新：仅当状态为 1 (待发货) 或 2 (待收货) 时才能变更为 3 (已完成)
        int rows = baseMapper.completeOrder(orderId, 3);
        if (rows == 0) {
            throw new ServiceException("订单当前状态不可确认收货");
        }
        return true;
    }

    /**
     * 根据主键 ID 获取订单基本详情
     */
    @Override
    public Order getOrderById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 模拟支付逻辑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, Long userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }

        // CAS 更新：仅当前状态为 0 (待付款) 时才能变更为 1 (待发货)
        int rows = baseMapper.payOrder(orderId, 1);
        if (rows == 0) {
            throw new ServiceException("订单当前状态不可支付");
        }
        return true;
    }

    /**
     * 更新订单状态（通用方法）
     */
    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        return baseMapper.updateStatus(orderId, status) > 0;
    }

    /**
     * 取消订单（买家主动行为）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, String reason, Long userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 仅买家允许取消
        if (!order.getBuyerId().equals(userId)) {
            throw new ServiceException("无权取消此订单");
        }

        // 状态检查：仅待付款 (0) 或待发货 (1) 时允许取消
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new ServiceException("当前订单状态不允许取消");
        }

        int rows = baseMapper.cancelOrder(orderId, reason);
        if (rows > 0) {
            // 恢复商品为上架状态
            productMapper.updateStatusCAS(order.getProductId(), 2, 1);
        }
        return rows > 0;
    }

    /**
     * 管理端全量分页获取系统订单
     */
    @Override
    public Map<String, Object> getAllOrders(Integer status, String keyword, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Order> list = baseMapper.selectAll(status, keyword, offset, pageSize);
        int total = baseMapper.selectAllCount(status, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public int countTodayOrder() {
        return baseMapper.countTodayOrder();
    }

    @Override
    public java.math.BigDecimal sumTodayAmount() {
        return baseMapper.sumTodayAmount();
    }

    /**
     * 生成唯一订单号
     * 格式：yyyyMMddHHmmss + 6位随机数
     * 这种生成算法保证了订单号在时间维度的唯一性，且随机数有效降低了毫秒级并发冲突的概率。
     */
    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        int random = new Random().nextInt(900000) + 100000; // 确保是100000~999999之间的6位数
        return dateStr + random;
    }
}

