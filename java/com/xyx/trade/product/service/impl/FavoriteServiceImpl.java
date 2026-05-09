package com.xyx.trade.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.product.domain.Favorite;
import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.mapper.FavoriteMapper;
import com.xyx.trade.product.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏服务实现类
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    /**
     * 收藏开关：如果已收藏则取消，如果未收藏则添加
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String toggleFavorite(Long productId, Long userId) {
        int count = favoriteMapper.checkIsFavorite(userId, productId);
        if (count > 0) {
            // 已存在记录，执行"取消收藏"
            favoriteMapper.deleteByUserAndProduct(userId, productId);
            return "已取消收藏";
        } else {
            // 不存在记录，执行"添加收藏"
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            baseMapper.insert(favorite);
            return "收藏成功";
        }
    }

    /**
     * 检查当前用户是否收藏了特定商品
     */
    @Override
    public boolean isFavorite(Long userId, Long productId) {
        if (userId == null)
            return false;
        return favoriteMapper.checkIsFavorite(userId, productId) > 0;
    }

    /**
     * 分页查询当前用户的收藏商品列表
     */
    @Override
    public Map<String, Object> getMyFavoriteProducts(Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        // 联表查询商品基本信息
        List<Product> list = favoriteMapper.selectFavoriteProductsByUserId(userId, offset, pageSize);
        int total = favoriteMapper.selectTotalCountByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 管理员分页查询全站收藏记录（跨表联查用户和商品信息）
     */
    @Override
    public Map<String, Object> getAllFavorites(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        // 调用 Mapper 返回灵活的 Map 结果集 - 使用正确的方法名
        List<Map<String, Object>> list = favoriteMapper.selectAllFavoritesWithPagination(offset, pageSize);
        int total = favoriteMapper.selectAllCount();

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }
}
