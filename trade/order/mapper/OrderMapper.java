package com.xyx.trade.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyx.trade.order.domain.Order;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM `xyx_order` WHERE id = #{id}")
    Order selectById(Long id);

    @Select("SELECT * FROM `xyx_order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);

    @SelectProvider(type = OrderSqlProvider.class, method = "selectMyOrders")
    List<Order> selectMyOrders(
        @Param("buyerId") Long buyerId, 
        @Param("sellerId") Long sellerId, 
        @Param("type") String type, 
        @Param("status") Integer status, 
        @Param("offset") int offset, 
        @Param("pageSize") int pageSize);

    @SelectProvider(type = OrderSqlProvider.class, method = "selectMyOrdersCount")
    int selectMyOrdersCount(
        @Param("buyerId") Long buyerId, 
        @Param("sellerId") Long sellerId, 
        @Param("type") String type, 
        @Param("status") Integer status);

    @SelectProvider(type = OrderSqlProvider.class, method = "selectAll")
    List<Order> selectAll(@Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @SelectProvider(type = OrderSqlProvider.class, method = "selectAllCount")
    int selectAllCount(@Param("status") Integer status, @Param("keyword") String keyword);

    @Insert("INSERT INTO `xyx_order`(order_no, buyer_id, seller_id, product_id, total_amount, status, payment_method, payment_time, buyer_remark, contact_phone, delivery_address) " +
            "VALUES(#{orderNo}, #{buyerId}, #{sellerId}, #{productId}, #{totalAmount}, #{status}, #{paymentMethod}, #{paymentTime}, #{buyerRemark}, #{contactPhone}, #{deliveryAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("UPDATE `xyx_order` SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE `xyx_order` SET status = 4, cancel_reason = #{reason} WHERE id = #{orderId}")
    int cancelOrder(@Param("orderId") Long orderId, @Param("reason") String reason);

    @Update("UPDATE `xyx_order` SET status = 3 WHERE id = #{orderId}")
    int completeOrder(@Param("orderId") Long orderId, @Param("status") int status);

    @Update("UPDATE `xyx_order` SET status = 1 WHERE id = #{orderId}")
    int payOrder(@Param("orderId") Long orderId, @Param("status") int status);

    @Select("SELECT SUM(total_amount) FROM `xyx_order` WHERE status IN (1, 2, 3)")
    java.math.BigDecimal sumTotalAmount();

    @Select("SELECT COUNT(*) FROM `xyx_order` WHERE DATE(create_time) = CURDATE()")
    int countTodayOrder();

    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM `xyx_order` WHERE status IN (1,2,3) AND DATE(create_time) = CURDATE()")
    java.math.BigDecimal sumTodayAmount();

    @Delete("DELETE FROM `xyx_order` WHERE id = #{id}")
    int deleteById(Long id);

    class OrderSqlProvider {
        public String selectMyOrders(
            @Param("buyerId") Long buyerId, 
            @Param("sellerId") Long sellerId, 
            @Param("type") String type, 
            @Param("status") Integer status, 
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize) {
            
            return new SQL() {{
                SELECT("o.*, p.title as product_name, ub.nickname as buyer_name, us.nickname as seller_name");
                FROM("`xyx_order` o");
                LEFT_OUTER_JOIN("xyx_product p ON o.product_id = p.id");
                LEFT_OUTER_JOIN("xyx_sys_user ub ON o.buyer_id = ub.id");
                LEFT_OUTER_JOIN("xyx_sys_user us ON o.seller_id = us.id");
                
                // 核心权限过滤：根据 type 参数决定查询条件
                if ("buy".equals(type)) {
                    // 我买的订单：只查 buyer_id = 当前用户
                    WHERE("o.buyer_id = #{buyerId}");
                } else if ("sell".equals(type)) {
                    // 我卖的订单：只查 seller_id = 当前用户
                    WHERE("o.seller_id = #{sellerId}");
                } else {
                    // 全部订单：查 buyer_id = 当前用户 或 seller_id = 当前用户
                    WHERE("(o.buyer_id = #{buyerId} OR o.seller_id = #{sellerId})");
                }
                
                if (status != null) {
                    WHERE("o.status = #{status}");
                }
                ORDER_BY("o.create_time DESC");
            }}.toString() + " LIMIT #{offset}, #{pageSize}";
        }

        public String selectMyOrdersCount(
            @Param("buyerId") Long buyerId, 
            @Param("sellerId") Long sellerId, 
            @Param("type") String type, 
            @Param("status") Integer status) {
            
            return new SQL() {{
                SELECT("COUNT(*)");
                FROM("`xyx_order`");
                
                // 核心权限过滤：根据 type 参数决定查询条件
                if ("buy".equals(type)) {
                    WHERE("buyer_id = #{buyerId}");
                } else if ("sell".equals(type)) {
                    WHERE("seller_id = #{sellerId}");
                } else {
                    WHERE("(buyer_id = #{buyerId} OR seller_id = #{sellerId})");
                }
                
                if (status != null) {
                    WHERE("status = #{status}");
                }
            }}.toString();
        }

        public String selectAll(@Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize) {
            return new SQL() {{
                SELECT("o.*, p.title as product_name, ub.nickname as buyer_name, us.nickname as seller_name");
                FROM("`xyx_order` o");
                LEFT_OUTER_JOIN("xyx_product p ON o.product_id = p.id");
                LEFT_OUTER_JOIN("xyx_sys_user ub ON o.buyer_id = ub.id");
                LEFT_OUTER_JOIN("xyx_sys_user us ON o.seller_id = us.id");
                if (status != null) {
                    WHERE("o.status = #{status}");
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    WHERE("(o.order_no LIKE CONCAT('%', #{keyword}, '%') OR p.title LIKE CONCAT('%', #{keyword}, '%') OR ub.nickname LIKE CONCAT('%', #{keyword}, '%') OR us.nickname LIKE CONCAT('%', #{keyword}, '%'))");
                }
                ORDER_BY("o.create_time DESC");
            }}.toString() + " LIMIT #{offset}, #{pageSize}";
        }

        public String selectAllCount(@Param("status") Integer status, @Param("keyword") String keyword) {
            return new SQL() {{
                SELECT("COUNT(*)");
                FROM("`xyx_order` o");
                LEFT_OUTER_JOIN("xyx_product p ON o.product_id = p.id");
                LEFT_OUTER_JOIN("xyx_sys_user ub ON o.buyer_id = ub.id");
                LEFT_OUTER_JOIN("xyx_sys_user us ON o.seller_id = us.id");
                if (status != null) {
                    WHERE("o.status = #{status}");
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    WHERE("(o.order_no LIKE CONCAT('%', #{keyword}, '%') OR p.title LIKE CONCAT('%', #{keyword}, '%') OR ub.nickname LIKE CONCAT('%', #{keyword}, '%') OR us.nickname LIKE CONCAT('%', #{keyword}, '%'))");
                }
            }}.toString();
        }
    }
}
