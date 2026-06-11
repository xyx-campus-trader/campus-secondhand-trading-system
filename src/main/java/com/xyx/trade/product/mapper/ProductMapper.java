package com.xyx.trade.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyx.trade.product.domain.Product;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @SelectProvider(type = ProductSqlProvider.class, method = "selectListPage")
    List<Product> selectListPage(
        @Param("keyword") String keyword, 
        @Param("categoryId") Long categoryId, 
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("condition") Integer condition, 
        @Param("campus") String campus, 
        @Param("sortBy") String sortBy, 
        @Param("order") String order, 
        @Param("offset") int offset, 
        @Param("pageSize") int pageSize);

    @SelectProvider(type = ProductSqlProvider.class, method = "selectTotalCount")
    int selectTotalCount(
        @Param("keyword") String keyword, 
        @Param("categoryId") Long categoryId, 
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("condition") Integer condition, 
        @Param("campus") String campus);

    @Select("SELECT p.*, u.nickname as seller_nickname FROM xyx_product p LEFT JOIN xyx_sys_user u ON p.user_id = u.id WHERE p.id = #{id}")
    Product selectById(Long id);

    @Select("SELECT * FROM xyx_product WHERE id = #{id} FOR UPDATE")
    Product selectByIdForUpdate(Long id);

    @Insert("INSERT INTO xyx_product(user_id, category_id, title, description, price, original_price, cover_image, images, status, `condition`, view_count, like_count, location, is_negotiable, is_delivery, campus, create_time) " +
            "VALUES(#{userId}, #{categoryId}, #{title}, #{description}, #{price}, #{originalPrice}, #{coverImage}, #{images}, #{status}, #{condition}, #{viewCount}, #{likeCount}, #{location}, #{isNegotiable}, #{isDelivery}, #{campus}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE xyx_product SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM xyx_product WHERE id = #{id}")
    int deleteById(Long id);

    @SelectProvider(type = ProductSqlProvider.class, method = "selectByUserId")
    List<Product> selectByUserId(
        @Param("userId") Long userId, 
        @Param("status") Integer status, 
        @Param("offset") int offset, 
        @Param("pageSize") int pageSize);

    @SelectProvider(type = ProductSqlProvider.class, method = "selectCountByUserId")
    int selectCountByUserId(
        @Param("userId") Long userId, 
        @Param("status") Integer status);

    @SelectProvider(type = ProductSqlProvider.class, method = "selectAllForAdmin")
    List<Product> selectAllForAdmin(@Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @SelectProvider(type = ProductSqlProvider.class, method = "selectAllCountForAdmin")
    int selectAllCountForAdmin(@Param("status") Integer status, @Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM xyx_product WHERE status = 1")
    int countActiveProducts();

    class ProductSqlProvider {
        public String selectListPage(
            @Param("keyword") String keyword, 
            @Param("categoryId") Long categoryId, 
            @Param("minPrice") BigDecimal minPrice, 
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("condition") Integer condition, 
            @Param("campus") String campus, 
            @Param("sortBy") String sortBy, 
            @Param("order") String order, 
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize) {
            
            // 字段白名单映射，防止 SQL 注入
            String orderField = "create_time"; // 默认按创建时间排序
            if ("price".equals(sortBy)) {
                orderField = "price";
            } else if ("view_count".equals(sortBy)) {
                orderField = "view_count";
            } else if ("like_count".equals(sortBy)) {
                orderField = "like_count";
            }
            final String finalOrderField = orderField;
            
            String orderDirection = "DESC";
            if ("asc".equalsIgnoreCase(order)) {
                orderDirection = "ASC";
            }
            final String finalOrderDirection = orderDirection;
            
            return new SQL() {{
                SELECT("p.*, u.nickname as seller_nickname");
                FROM("xyx_product p");
                LEFT_OUTER_JOIN("xyx_sys_user u ON p.user_id = u.id");
                if (keyword != null && !keyword.isEmpty()) {
                    WHERE("p.title LIKE CONCAT('%', #{keyword}, '%')");
                }
                if (categoryId != null) {
                    WHERE("p.category_id = #{categoryId}");
                }
                if (minPrice != null) {
                    WHERE("p.price >= #{minPrice}");
                }
                if (maxPrice != null) {
                    WHERE("p.price <= #{maxPrice}");
                }
                if (condition != null) {
                    WHERE("p.`condition` = #{condition}");
                }
                if (campus != null) {
                    WHERE("p.campus = #{campus}");
                }
                ORDER_BY("p." + finalOrderField + " " + finalOrderDirection);
            }}.toString() + " LIMIT #{offset}, #{pageSize}";
        }

        public String selectTotalCount(
            @Param("keyword") String keyword, 
            @Param("categoryId") Long categoryId, 
            @Param("minPrice") BigDecimal minPrice, 
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("condition") Integer condition, 
            @Param("campus") String campus) {
            return new SQL() {{
                SELECT("COUNT(*)");
                FROM("xyx_product");
                if (keyword != null && !keyword.isEmpty()) {
                    WHERE("title LIKE CONCAT('%', #{keyword}, '%')");
                }
                if (categoryId != null) {
                    WHERE("category_id = #{categoryId}");
                }
                if (minPrice != null) {
                    WHERE("price >= #{minPrice}");
                }
                if (maxPrice != null) {
                    WHERE("price <= #{maxPrice}");
                }
                if (condition != null) {
                    WHERE("`condition` = #{condition}");
                }
                if (campus != null) {
                    WHERE("campus = #{campus}");
                }
            }}.toString();
        }

        public String selectByUserId(
            @Param("userId") Long userId, 
            @Param("status") Integer status, 
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize) {
            return new SQL() {{
                SELECT("p.*, u.nickname as seller_nickname");
                FROM("xyx_product p");
                LEFT_OUTER_JOIN("xyx_sys_user u ON p.user_id = u.id");
                WHERE("p.user_id = #{userId}");
                if (status != null) {
                    WHERE("p.status = #{status}");
                }
                ORDER_BY("p.create_time DESC");
            }}.toString() + " LIMIT #{offset}, #{pageSize}";
        }

        public String selectCountByUserId(
            @Param("userId") Long userId, 
            @Param("status") Integer status) {
            return new SQL() {{
                SELECT("COUNT(*)");
                FROM("xyx_product");
                WHERE("user_id = #{userId}");
                if (status != null) {
                    WHERE("status = #{status}");
                }
            }}.toString();
        }

        public String selectAllForAdmin(@Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize) {
            return new SQL() {{
                SELECT("p.*, u.nickname as seller_nickname");
                FROM("xyx_product p");
                LEFT_OUTER_JOIN("xyx_sys_user u ON p.user_id = u.id");
                if (status != null) {
                    WHERE("p.status = #{status}");
                }
                if (keyword != null && !keyword.isEmpty()) {
                    WHERE("p.title LIKE CONCAT('%', #{keyword}, '%')");
                }
                ORDER_BY("p.create_time DESC");
            }}.toString() + " LIMIT #{offset}, #{pageSize}";
        }

        public String selectAllCountForAdmin(@Param("status") Integer status, @Param("keyword") String keyword) {
            return new SQL() {{
                SELECT("COUNT(*)");
                FROM("xyx_product");
                if (status != null) {
                    WHERE("status = #{status}");
                }
                if (keyword != null && !keyword.isEmpty()) {
                    WHERE("title LIKE CONCAT('%', #{keyword}, '%')");
                }
            }}.toString();
        }
    }
}


