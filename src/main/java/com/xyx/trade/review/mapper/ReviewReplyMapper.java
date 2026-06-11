package com.xyx.trade.review.mapper;

import com.xyx.trade.review.domain.ReviewReply;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价回复 Mapper 接口
 */
@Mapper
public interface ReviewReplyMapper {

    /**
     * 新增回复
     */
    @Insert("INSERT INTO xyx_review_reply(review_id, user_id, content, create_time) " +
            "VALUES(#{reviewId}, #{userId}, #{content}, NOW())")
    int insert(@Param("reviewId") Long reviewId, @Param("userId") Long userId, @Param("content") String content);

    /**
     * 根据评价ID查询所有回复
     */
    @Select("SELECT * FROM xyx_review_reply WHERE review_id = #{reviewId} ORDER BY create_time ASC")
    List<ReviewReply> selectByReviewId(@Param("reviewId") Long reviewId);
}
