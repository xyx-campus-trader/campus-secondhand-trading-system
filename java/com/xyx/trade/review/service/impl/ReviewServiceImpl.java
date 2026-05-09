package com.xyx.trade.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.order.domain.Order;
import com.xyx.trade.order.mapper.OrderMapper;
import com.xyx.trade.review.domain.Review;
import com.xyx.trade.review.domain.ReviewReply;
import com.xyx.trade.review.mapper.ReviewMapper;
import com.xyx.trade.review.mapper.ReviewReplyMapper;
import com.xyx.trade.review.service.ReviewService;
import com.xyx.trade.user.domain.User;
import com.xyx.trade.user.exception.ServiceException;
import com.xyx.trade.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReviewReplyMapper reviewReplyMapper;

    /**
     * 发布评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReview(Review review) {
        // 1. 获取订单，基础业务对象校验
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 2. 参与者身份识别
        boolean isBuyer = order.getBuyerId().equals(review.getReviewerId());
        boolean isSeller = order.getSellerId().equals(review.getReviewerId());

        if (!isBuyer && !isSeller) {
            throw new ServiceException("无权评价此订单");
        }

        // 3. 状态前置检查：防止在待付款或待发货阶段进行评价
        if (order.getStatus() != 3) { // 3:已完成
            throw new ServiceException("订单未完成，无法评价");
        }

        // 4. 定向逻辑：买家评卖家，或卖家评买家
        if (isBuyer) {
            review.setTargetId(order.getSellerId());
            review.setType(0); // 0: 买家评卖家
        } else {
            review.setTargetId(order.getBuyerId());
            review.setType(1); // 1: 卖家评买家
        }

        // 5. 唯一性检查：每个角色对每个订单仅能评价一次
        Review exist = baseMapper.selectByOrderAndReviewer(review.getOrderId(), review.getReviewerId());
        if (exist != null) {
            throw new ServiceException("您已评价过该订单");
        }

        // 6. 持久化存储
        baseMapper.insert(review);
        return review.getId();
    }

    /**
     * 修改个人评价
     * 仅允许评价人本人修改评价内容或分数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReview(Review review, Long userId) {
        Review exist = baseMapper.selectById(review.getId());
        if (exist == null) {
            throw new ServiceException("评价不存在");
        }
        // 严格权限：非本人不可修改
        if (!exist.getReviewerId().equals(userId)) {
            throw new ServiceException("无权修改此评价");
        }

        int rows = baseMapper.update(review);
        return rows > 0;
    }

    /**
     * 删除评价
     * 权限场景：
     * 1. 评价人：可以撤回自己的评价。
     * 2. 管理员：可以删除涉嫌违规或恶意评价的内容。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReview(Long id, Long userId) {
        Review exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("评价不存在");
        }

        User user = userMapper.selectById(userId);
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());

        // 二元权限模型校验
        if (!exist.getReviewerId().equals(userId) && !isAdmin) {
            throw new ServiceException("无权删除此评价");
        }

        int rows = baseMapper.deleteById(id);
        return rows > 0;
    }

    /**
     * 获取评价列表（含用户信息脱敏逻辑）
     * 
     * @param targetId   被评价人ID
     * @param reviewerId 评价人ID
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return 分页结果及处理后的评价数据
     */
    @Override
    public Map<String, Object> getReviewList(Long targetId, Long reviewerId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;

        List<Review> list = baseMapper.selectList(targetId, reviewerId, offset, pageSize);
        int total = baseMapper.selectCount(targetId, reviewerId);

        // 填充用户信息，并处理【匿名评论】的显示逻辑，同时加载回复列表
        for (Review review : list) {
            User reviewer = userMapper.selectById(review.getReviewerId());
            if (review.getIsAnonymous() == 1) {
                // 如果用户选择了匿名，在这里对用户信息进行"模糊化"处理，保护个人隐私
                if (reviewer != null) {
                    reviewer.setNickname("匿名用户");
                    reviewer.setAvatarUrl(null); // 前端会兜底逻辑显示默认头像
                }
            }
            review.setReviewer(reviewer);
            
            // 加载该评价的所有回复
            List<ReviewReply> replies = reviewReplyMapper.selectByReviewId(review.getId());
            review.setReplyList(replies);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 根据 ID 查询特定评价详情
     */
    @Override
    public Review getReviewById(Long id) {
        Review review = baseMapper.selectById(id);
        if (review != null) {
            // 加载该评价的所有回复
            List<ReviewReply> replies = reviewReplyMapper.selectByReviewId(review.getId());
            review.setReplyList(replies);
        }
        return review;
    }

    /**
     * 管理员查询所有评价
     */
    public Map<String, Object> getAdminReviewList(Long productId, Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Review> list = baseMapper.selectAdminList(productId, userId, offset, pageSize);
        int total = baseMapper.selectAdminCount(productId, userId);

        // 填充用户信息，同时加载回复列表
        for (Review review : list) {
            User reviewer = userMapper.selectById(review.getReviewerId());
            if (review.getIsAnonymous() == 1) {
                if (reviewer != null) {
                    reviewer.setNickname("匿名用户");
                    reviewer.setAvatarUrl(null);
                }
            }
            review.setReviewer(reviewer);

            // 填充被评价人昵称
            User targetUser = userMapper.selectById(review.getTargetId());
            if (targetUser != null) {
                review.setTargetNickname(targetUser.getNickname());
            } else {
                review.setTargetNickname("未知用户");
            }

            // 加载该评价的所有回复
            List<ReviewReply> replies = reviewReplyMapper.selectByReviewId(review.getId());
            review.setReplyList(replies);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 管理员删除评价
     */
    public boolean deleteReviewByAdmin(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    /**
     * 管理员回复评价
     * 插入新回复到 reply 表，同时追加到 adminReply 字段（不覆盖旧内容）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminReplyReview(Long id, String adminReply, Long userId) {
        int rows = reviewReplyMapper.insert(id, userId, adminReply);
        appendReplyToAdminReplyField(id, adminReply);
        return rows > 0;
    }

    /**
     * 追加回复内容到 adminReply 字段
     */
    public boolean appendReplyToAdminReplyField(Long id, String newReplyContent) {
        Review review = baseMapper.selectById(id);
        if (review == null) return false;
        String oldReply = review.getAdminReply() == null ? "" : review.getAdminReply();
        String separator = oldReply.isEmpty() ? "" : "\n【管理员回复】\n";
        String updatedReply = oldReply + separator + newReplyContent;
        return baseMapper.appendAdminReply(id, updatedReply) > 0;
    }

    /**
     * 获取评价的所有回复
     */
    public List<ReviewReply> getReviewReplies(Long reviewId) {
        return reviewReplyMapper.selectByReviewId(reviewId);
    }
}

