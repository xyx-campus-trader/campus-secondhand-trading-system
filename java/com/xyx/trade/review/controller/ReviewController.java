package com.xyx.trade.review.controller;

import com.xyx.trade.review.domain.Review;
import com.xyx.trade.review.service.ReviewService;
import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 */
@Api(tags = "评价管理")
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 发布评价
     */
    @ApiOperation(value = "发布评价", notes = "对已完成的订单进行评价")
    @PostMapping("/create")
    public AjaxResult createReview(@RequestBody CreateReviewRequest requestBody, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Review review = new Review();
        review.setReviewerId(userId);
        review.setOrderId(requestBody.getOrderId());
        review.setRating(requestBody.getRating());
        review.setContent(requestBody.getContent());
        review.setTags(requestBody.getTags() != null ? String.join(",", requestBody.getTags()) : null);
        review.setIsAnonymous(requestBody.getIsAnonymous() ? 1 : 0);

        Long reviewId = reviewService.createReview(review);
        return AjaxResult.success("评价成功", reviewId);
    }

    /**
     * 修改评价
     */
    @ApiOperation(value = "修改评价", notes = "修改已发布的评价")
    @PutMapping("/update")
    public AjaxResult updateReview(@RequestBody UpdateReviewRequest requestBody, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Review review = new Review();
        review.setId(requestBody.getId());
        review.setRating(requestBody.getRating());
        review.setContent(requestBody.getContent());
        review.setTags(requestBody.getTags() != null ? String.join(",", requestBody.getTags()) : null);
        review.setIsAnonymous(requestBody.getIsAnonymous() ? 1 : 0);

        boolean success = reviewService.updateReview(review, userId);
        if (success) {
            return AjaxResult.success("修改成功");
        } else {
            return AjaxResult.error("修改失败");
        }
    }

    /**
     * 删除评价
     */
    @ApiOperation(value = "删除评价", notes = "删除已发布的评价")
    @ApiImplicitParam(name = "id", value = "评价ID", required = true, dataType = "long", paramType = "path")
    @DeleteMapping("/delete/{id}")
    public AjaxResult deleteReview(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        boolean success = reviewService.deleteReview(id, userId);
        if (success) {
            return AjaxResult.success("删除成功");
        } else {
            return AjaxResult.error("删除失败");
        }
    }

    /**
     * 获取评价列表
     */
    @ApiOperation(value = "获取评价列表", notes = "查看某人收到的评价或发出的评价")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetId", value = "被评价者 ID（查看某人收到的评价）", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "reviewerId", value = "评价者 ID（查看某人发出的评价）", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码，默认 1", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量，默认 10", dataType = "int", paramType = "query")
    })
    @GetMapping("/list")
    public AjaxResult getReviewList(
            Long targetId,
            Long reviewerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Map<String, Object> result = reviewService.getReviewList(targetId, reviewerId, pageNum, pageSize);
        return AjaxResult.success(result);
    }

    /**
     * 管理员接口：查询所有评价
     */
    @ApiOperation(value = "管理员查询所有评价", notes = "查看全站评价列表，支持按商品、用户筛选")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "productId", value = "商品 ID", dataType = "long", paramType = "query"),
        @ApiImplicitParam(name = "userId", value = "用户 ID", dataType = "long", paramType = "query"),
        @ApiImplicitParam(name = "pageNum", value = "页码，默认 1", dataType = "int", paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页数量，默认 10", dataType = "int", paramType = "query")
    })
    @GetMapping("/admin/list")
    public AjaxResult getAdminReviewList(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Map<String, Object> result = reviewService.getAdminReviewList(productId, userId, pageNum, pageSize);
        return AjaxResult.success(result);
    }

    /**
     * 管理员接口：删除评价（无需权限校验，由拦截器统一处理）
     */
    @ApiOperation(value = "管理员删除评价", notes = "删除违规评价")
    @ApiImplicitParam(name = "id", value = "评价 ID", required = true, dataType = "long", paramType = "path")
    @DeleteMapping("/admin/delete/{id}")
    public AjaxResult adminDeleteReview(@PathVariable Long id) {
        boolean success = reviewService.deleteReviewByAdmin(id);
        if (success) {
            return AjaxResult.success("删除成功");
        } else {
            return AjaxResult.error("删除失败");
        }
    }

    /**
     * 管理员接口：回复评价
     */
    @ApiOperation(value = "管理员回复评价", notes = "对评价进行回复")
    @PostMapping("/admin/reply")
    public AjaxResult adminReplyReview(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = Long.valueOf(requestBody.get("id").toString());
        String adminReply = requestBody.get("adminReply").toString();

        boolean success = reviewService.adminReplyReview(id, adminReply, userId);
        if (success) {
            return AjaxResult.success("回复成功");
        } else {
            return AjaxResult.error("回复失败");
        }
    }

    /**
     * 发布评价请求对象
     */
    @ApiModel(description = "发布评价请求对象")
    public static class CreateReviewRequest {
        @ApiModelProperty(value = "订单ID", required = true, example = "1")
        private Long orderId;

        @ApiModelProperty(value = "评分（1-5星）", required = true, example = "5")
        private Integer rating;

        @ApiModelProperty(value = "评价内容", example = "东西很好")
        private String content;

        @ApiModelProperty(value = "评价标签")
        private List<String> tags;

        @ApiModelProperty(value = "是否匿名，默认false", example = "false")
        private Boolean isAnonymous = false;

        // Getters and Setters
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public Boolean getIsAnonymous() {
            return isAnonymous;
        }

        public void setIsAnonymous(Boolean anonymous) {
            isAnonymous = anonymous;
        }
    }

    /**
     * 修改评价请求对象
     */
    @ApiModel(description = "修改评价请求对象")
    public static class UpdateReviewRequest {
        @ApiModelProperty(value = "评价ID", required = true, example = "1")
        private Long id;

        @ApiModelProperty(value = "评分（1-5星）", example = "5")
        private Integer rating;

        @ApiModelProperty(value = "评价内容", example = "东西超好")
        private String content;

        @ApiModelProperty(value = "评价标签")
        private List<String> tags;

        @ApiModelProperty(value = "是否匿名", example = "false")
        private Boolean isAnonymous = false;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public Boolean getIsAnonymous() {
            return isAnonymous;
        }

        public void setIsAnonymous(Boolean anonymous) {
            isAnonymous = anonymous;
        }
    }
}

