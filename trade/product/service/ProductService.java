package com.xyx.trade.product.service;

import com.xyx.trade.product.domain.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 发布商品
     */
    Long createProduct(Product product);

    /**
     * 编辑商品
     */
    boolean updateProduct(Product product);

    /**
     * 删除商品
     */
    boolean deleteProduct(Long id);

    /**
     * 获取商品详情
     */
    Product getProductById(Long id);

    /**
     * 获取商品列表
     */
    Map<String, Object> getProductList(
            Long categoryId, String keyword, String campus, Integer condition,
            Double minPrice, Double maxPrice, String sortBy, String order,
            Integer pageNum, Integer pageSize);

    /**
     * 获取我的商品
     */
    Map<String, Object> getMyProducts(Integer pageNum, Integer pageSize);

    /**
     * 管理员获取商品列表
     */
    Map<String, Object> getAdminProductList(Map<String, Object> params);

    /**
     * 管理员删除商品
     */
    boolean adminDeleteProduct(Long id);

    /**
     * 选择商品列表
     */
    Map<String, Object> selectProductList(int pageNum, int pageSize, String keyword, Long categoryId,
                                         BigDecimal minPrice, BigDecimal maxPrice, Integer condition, String campus, String sortBy, String order);

    /**
     * 选择商品详情
     */
    Product selectProductById(Long id, Long userId);

    /**
     * 发布商品
     */
    Long publishProduct(Product product);

    /**
     * 选择用户商品列表
     */
    Map<String, Object> selectUserProductList(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 更新商品状态
     */
    boolean updateProductStatus(Long id, Integer status, Long userId);

    /**
     * 切换收藏状态
     */
    String toggleFavorite(Long productId, Long userId);

    /**
     * 管理员更新商品状态
     */
    boolean updateProductStatusForAdmin(Long id, Integer status);

    /**
     * 选择所有商品列表
     */
    Map<String, Object> selectAllProductList(Integer status, String keyword, int pageNum, int pageSize);

    /**
     * 管理员获取所有商品（带搜索）
     */
    List<Product> selectAllForAdmin(Integer status, String keyword, int offset, int pageSize);

    /**
     * 管理员获取所有商品数量（带搜索）
     */
    int selectAllCountForAdmin(Integer status, String keyword);

    /**
     * AI智能推荐商品
     */
    List<Product> aiRecommend(Long userId);
}

