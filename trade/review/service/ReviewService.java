package com.xyx.trade.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyx.trade.review.domain.Review;

import java.util.Map;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 创建评价
     */
    Long createReview(Review review);

    /**
     * 修改评价
     */
    boolean updateReview(Review review, Long userId);

    /**
     * 删除评价
     */
    boolean deleteReview(Long id, Long userId);

    /**
     * 获取评价列表
     */
    Map<String, Object> getReviewList(Long targetId, Long reviewerId, int pageNum, int pageSize);

    /**
     * 根据 ID 获取评价
     */
    Review getReviewById(Long id);

    /**
     * 管理员查询所有评价
     */
    Map<String, Object> getAdminReviewList(Long productId, Long userId, int pageNum, int pageSize);

    /**
     * 管理员删除评价
     */
    boolean deleteReviewByAdmin(Long id);

    /**
     * 管理员回复评价
     */
    boolean adminReplyReview(Long id, String adminReply, Long userId);
}

