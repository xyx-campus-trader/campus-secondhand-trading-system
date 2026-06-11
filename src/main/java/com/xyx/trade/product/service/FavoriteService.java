package com.xyx.trade.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyx.trade.product.domain.Favorite;

import java.util.Map;

/**
 * 收藏服务接口
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 收藏/取消收藏商品
     * 
     * @return 最新的收藏状态信息
     */
    String toggleFavorite(Long productId, Long userId);

    /**
     * 检查用户是否收藏了该商品
     */
    boolean isFavorite(Long userId, Long productId);

    /**
     * 获取用户收藏的商品列表
     */
    Map<String, Object> getMyFavoriteProducts(Long userId, int pageNum, int pageSize);

    /**
     * 管理员获取全站收藏列表
     */
    Map<String, Object> getAllFavorites(int pageNum, int pageSize);
}

