package com.xyx.trade.product.controller;

import com.xyx.trade.product.service.FavoriteService;
import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 收藏控制器
 */
@Api(tags = "收藏管理")
@RestController
@RequestMapping("/api/product")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 切换收藏状态
     */
    @ApiOperation(value = "切换收藏状态", notes = "收藏或取消收藏商品")
    @ApiImplicitParam(name = "id", value = "商品ID", required = true, dataType = "long", paramType = "path")
    @PostMapping("/favorite/{id}")
    public AjaxResult<String> toggleFavorite(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return AjaxResult.error("请先登录");
        }

        String result = favoriteService.toggleFavorite(id, userId);
        return AjaxResult.success(result);
    }

    /**
     * 获取收藏列表
     */
    @ApiOperation(value = "获取收藏列表", notes = "获取当前用户的收藏商品列表")
    @GetMapping("/favorite/list")
    public AjaxResult<Map<String, Object>> getFavoriteList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return AjaxResult.error("请先登录");
        }

        Map<String, Object> result = favoriteService.getMyFavoriteProducts(userId, pageNum, pageSize);
        return AjaxResult.success(result);
    }
}
