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

import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    /** 每种操作允许的订单状态 */
    private static final List<Integer> PAY_ALLOWED       = List.of(0);      // 待付款
    private static final List<Integer> CANCEL_ALLOWED    = List.of(0, 1);   // 待付款、待发货
    private static final List<Integer> COMPLETE_ALLOWED  = List.of(2);      // 待收货

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

        // 4. 标记商品已售出（FOR UPDATE 锁保证并发安全）
        productMapper.updateStatus(order.getProductId(), 2);

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

        // 批量填充商品信息（避免 N+1 查询）
        java.util.Set<Long> productIds = list.stream()
                .map(Order::getProductId)
                .collect(java.util.stream.Collectors.toSet());
        if (!productIds.isEmpty()) {
            java.util.Map<Long, Product> productMap = productMapper.selectBatchIds(productIds)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
            for (Order order : list) {
                order.setProduct(productMap.get(order.getProductId()));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 确认收货（完成订单，SELECT FOR UPDATE 防止并发）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId, Long userId) {
        Order order = baseMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }

        // 只有待收货(2)状态才能确认收货
        if (!COMPLETE_ALLOWED.contains(order.getStatus())) {
            throw new ServiceException("订单当前状态不可确认收货");
        }

        // 修改订单为已完成状态
        int rows = baseMapper.completeOrder(orderId, 3);
        if (rows > 0) {
            // 订单完成意味着商品物理权属转移，将商品标记为已售出
            productMapper.updateStatus(order.getProductId(), 2);
            return true;
        }
        return false;
    }

    /**
     * 根据主键 ID 获取订单基本详情
     */
    @Override
    public Order getOrderById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 模拟支付逻辑（SELECT FOR UPDATE 悲观锁防止并发状态错乱）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, Long userId) {
        // FOR UPDATE 锁定订单行，防止并发支付/取消/收货
        Order order = baseMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        if (!PAY_ALLOWED.contains(order.getStatus())) {
            throw new ServiceException("订单当前状态不可支付");
        }

        return baseMapper.payOrder(orderId, 1) > 0;
    }

    /**
     * 更新订单状态（管理端使用，FOR UPDATE + 状态校验 + 商品状态同步）
     * 仅供管理员调用，状态流转需符合业务规则
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long orderId, Integer status) {
        Order order = baseMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 校验状态流转合法性（与用户端保持一致）
        int oldStatus = order.getStatus();
        if (oldStatus == 3 || oldStatus == 4) {
            throw new ServiceException("终态订单不可修改状态");
        }

        boolean result = baseMapper.updateStatus(orderId, status) > 0;
        if (result) {
            // 订单完成 → 商品标记已售出
            if (status == 3) {
                productMapper.updateStatus(order.getProductId(), 2);
            }
            // 订单取消 → 商品恢复上架
            if (status == 4) {
                productMapper.updateStatus(order.getProductId(), 1);
            }
        }
        return result;
    }

    /**
     * 取消订单（买家主动行为，SELECT FOR UPDATE 防止并发）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, String reason, Long userId) {
        Order order = baseMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 仅买家允许取消
        if (!order.getBuyerId().equals(userId)) {
            throw new ServiceException("无权取消此订单");
        }

        // 状态检查：仅待付款 (0) 或待发货 (1) 时允许取消
        if (!CANCEL_ALLOWED.contains(order.getStatus())) {
            throw new ServiceException("当前订单状态不允许取消");
        }

        boolean result = baseMapper.cancelOrder(orderId, reason) > 0;
        // 取消订单后商品恢复上架
        if (result) {
            productMapper.updateStatus(order.getProductId(), 1);
        }
        return result;
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
     * 定时扫描超时未付订单，每分钟执行一次。
     * 超过 30 分钟未支付的待付款订单将被自动取消，商品恢复上架。
     */
    @Scheduled(fixedDelay = 60000)
    public void cancelExpiredOrders() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -30);
        Date deadline = cal.getTime();

        List<Order> expired = baseMapper.selectUnpaidBefore(deadline);
        if (expired.isEmpty()) {
            return;
        }

        log.info("发现 {} 个超时未付订单，开始自动取消", expired.size());
        for (Order o : expired) {
            try {
                autoCancelOrder(o);
            } catch (Exception e) {
                log.error("自动取消订单失败, orderId={}, orderNo={}", o.getId(), o.getOrderNo(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void autoCancelOrder(Order order) {
        Order locked = baseMapper.selectByIdForUpdate(order.getId());
        if (locked == null || locked.getStatus() != 0) {
            return;
        }
        baseMapper.cancelOrder(locked.getId(), "超时未支付，系统自动取消");
        productMapper.updateStatus(locked.getProductId(), 1);
        log.info("超时订单自动取消成功, orderId={}, productId={}", locked.getId(), locked.getProductId());
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

