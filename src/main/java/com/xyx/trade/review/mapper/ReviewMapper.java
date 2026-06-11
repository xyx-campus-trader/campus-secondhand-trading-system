package com.xyx.trade.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyx.trade.review.domain.Review;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT * FROM xyx_review WHERE id = #{id}")
    Review selectById(Long id);

    @Select("SELECT * FROM xyx_review WHERE order_id = #{orderId} AND reviewer_id = #{reviewerId}")
    Review selectByOrderAndReviewer(@Param("orderId") Long orderId, @Param("reviewerId") Long reviewerId);

    @SelectProvider(type = ReviewSqlProvider.class, method = "selectList")
    List<Review> selectList(
        @Param("targetId") Long targetId, 
        @Param("reviewerId") Long reviewerId, 
        @Param("offset") int offset, 
        @Param("pageSize") int pageSize);

    @SelectProvider(type = ReviewSqlProvider.class, method = "selectCount")
    int selectCount(
        @Param("targetId") Long targetId, 
        @Param("reviewerId") Long reviewerId);

    @SelectProvider(type = ReviewSqlProvider.class, method = "selectAdminList")
    List<Review> selectAdminList(
        @Param("productId") Long productId, 
        @Param("userId") Long userId, 
        @Param("offset") int offset, 
        @Param("pageSize") int pageSize);

    @SelectProvider(type = ReviewSqlProvider.class, method = "selectAdminCount")
    int selectAdminCount(
        @Param("productId") Long productId, 
        @Param("userId") Long userId);

    @Insert("INSERT INTO xyx_review(order_id, reviewer_id, target_id, type, rating, content, tags, is_anonymous, create_time) " +
            "VALUES(#{orderId}, #{reviewerId}, #{targetId}, #{type}, #{rating}, #{content}, #{tags}, #{isAnonymous}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);

    @UpdateProvider(type = ReviewSqlProvider.class, method = "update")
    int update(Review review);

    @Delete("DELETE FROM xyx_review WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE xyx_review SET admin_reply = #{adminReply}, reply_time = NOW() WHERE id = #{id}")
    int adminReplyReview(@Param("id") Long id, @Param("adminReply") String adminReply);

    @Update("UPDATE xyx_review SET admin_reply = #{adminReply}, reply_time = NOW() WHERE id = #{id}")
    int appendAdminReply(@Param("id") Long id, @Param("adminReply") String adminReply);

    @Select("SELECT COUNT(*) FROM xyx_review")
    int countAllReviews();

    class ReviewSqlProvider {

        public String selectList(
            @Param("targetId") Long targetId, 
            @Param("reviewerId") Long reviewerId, 
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize) {
            return new SQL() {{
                SELECT("*");
                FROM("xyx_review");
                if (targetId != null) {
                    WHERE("target_id = #{targetId}");
                }
                if (reviewerId != null) {
                    WHERE("reviewer_id = #{reviewerId}");
                }
                ORDER_BY("create_time DESC");
            }}.toString() + " LIMIT #{offset}, #{pageSize}";
        }

        public String selectCount(
            @Param("targetId") Long targetId, 
            @Param("reviewerId") Long reviewerId) {
            return new SQL() {{
                SELECT("COUNT(*)");
                FROM("xyx_review");
                if (targetId != null) {
                    WHERE("target_id = #{targetId}");
                }
                if (reviewerId != null) {
                    WHERE("reviewer_id = #{reviewerId}");
                }
            }}.toString();
        }

        public String selectAdminList(
            @Param("productId") Long productId, 
            @Param("userId") Long userId, 
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize) {
            return new SQL() {{
                SELECT("r.*, u.username as reviewerUsername, u.nickname as reviewerNickname, p.title as productName");
                FROM("xyx_review r");
                LEFT_OUTER_JOIN("xyx_sys_user u ON r.reviewer_id = u.id");
                LEFT_OUTER_JOIN("xyx_order o ON r.order_id = o.id");
                LEFT_OUTER_JOIN("xyx_product p ON o.product_id = p.id");
                if (productId != null) {
                    WHERE("o.product_id = #{productId}");
                }
                if (userId != null) {
                    WHERE("(r.reviewer_id = #{userId} OR r.target_id = #{userId})");
                }
                ORDER_BY("r.create_time DESC");
            }}.toString() + " LIMIT #{offset}, #{pageSize}";
        }

        public String selectAdminCount(
            @Param("productId") Long productId, 
            @Param("userId") Long userId) {
            return new SQL() {{
                SELECT("COUNT(*)");
                FROM("xyx_review r");
                if (productId != null) {
                    WHERE("r.order_id IN (SELECT id FROM `xyx_order` WHERE product_id = #{productId})");
                }
                if (userId != null) {
                    WHERE("(r.reviewer_id = #{userId} OR r.target_id = #{userId})");
                }
            }}.toString();
        }

        public String update(Review review) {
            return new SQL() {{
                UPDATE("xyx_review");
                if (review.getRating() != null) {
                    SET("rating = #{rating}");
                }
                if (review.getContent() != null) {
                    SET("content = #{content}");
                }
                if (review.getTags() != null) {
                    SET("tags = #{tags}");
                }
                if (review.getIsAnonymous() != null) {
                    SET("is_anonymous = #{isAnonymous}");
                }
                WHERE("id = #{id}");
            }}.toString();
        }
    }
}
