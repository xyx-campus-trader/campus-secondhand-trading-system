package com.xyx.trade.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.mapper.ProductMapper;
import com.xyx.trade.product.service.ProductService;
import com.xyx.trade.user.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_INFO_PREFIX = "product:info:";
    private static final String PRODUCT_LIST_PREFIX = "product:list:";

    // ==================== ZSet：商品浏览量排行 ====================
    // key → product:views:rank，score = 浏览量，member = productId
    // ZINCRBY product:views:rank 1 {productId}  每次查看加1
    // ZREVRANGE product:views:rank 0 9           取 Top 10
    private static final String PRODUCT_VIEWS_RANK = "product:views:rank";

    // ==================== Set：用户收藏 ====================
    // key → user:likes:{userId}，member = productId
    // SADD user:likes:15 101     收藏商品 101
    // SREM user:likes:15 101     取消收藏
    // SISMEMBER user:likes:15 101 判断是否已收藏
    // SCARD user:likes:15        收藏总数
    // SINTER user:likes:15 user:likes:16  共同收藏
    private static final String USER_LIKES_PREFIX = "user:likes:";

    // ==================== Hash：分类商品数量统计 ====================
    // key → product:category:stats
    // field = categoryId, value = 该分类下的商品数量
    // HINCRBY product:category:stats 1 1    分类 1 商品数 +1
    // HGETALL product:category:stats         拿全部分类统计
    private static final String CATEGORY_STATS_KEY = "product:category:stats";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("price", "createTime");

    @Override
    public Long createProduct(Product product) {
        Long id = baseMapper.insert(product) > 0 ? product.getId() : null;
        if (id != null && product.getCategoryId() != null) {
            // Hash：分类商品数 +1
            redisTemplate.opsForHash().increment(CATEGORY_STATS_KEY,
                    product.getCategoryId().toString(), 1);
        }
        return id;
    }

    @Override
    public boolean updateProduct(Product product) {
        boolean rows = baseMapper.updateById(product) > 0;
        if (rows) {
            String key = PRODUCT_INFO_PREFIX + product.getId();
            redisTemplate.delete(key);
        }
        return rows;
    }

    @Override
    public boolean deleteProduct(Long id) {
        Product product = baseMapper.selectById(id);
        boolean rows = baseMapper.deleteById(id) > 0;
        if (rows) {
            String key = PRODUCT_INFO_PREFIX + id;
            redisTemplate.delete(key);
            // ZSet：移除商品浏览量记录
            redisTemplate.opsForZSet().remove(PRODUCT_VIEWS_RANK, id.toString());
            // Hash：分类商品数 -1
            if (product != null && product.getCategoryId() != null) {
                redisTemplate.opsForHash().increment(CATEGORY_STATS_KEY,
                        product.getCategoryId().toString(), -1);
            }
        }
        return rows;
    }

    @Override
    public Product getProductById(Long id) {
        String key = PRODUCT_INFO_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached == null) {
            Product product = baseMapper.selectById(id);
            if (product != null) {
                redisTemplate.opsForValue().set(key, product, 1, TimeUnit.HOURS);
            } else {
                // 空值缓存防穿透，存标记 5 分钟过期
                redisTemplate.opsForValue().set(key, "NULL", 5, TimeUnit.MINUTES);
            }
            if (product != null) {
                redisTemplate.opsForZSet().incrementScore(PRODUCT_VIEWS_RANK, id.toString(), 1);
            }
            return product;
        }

        if (cached instanceof Product) {
            Product product = (Product) cached;
            redisTemplate.opsForZSet().incrementScore(PRODUCT_VIEWS_RANK, id.toString(), 1);
            return product;
        }

        // 命中空值标记，防穿透
        return null;
    }

    @Override
    public Map<String, Object> getProductList(Long categoryId, String keyword, String campus, Integer condition,
                                               Double minPrice, Double maxPrice, String sortBy, String order,
                                               Integer pageNum, Integer pageSize) {
        return selectProductList(pageNum, pageSize, keyword, categoryId,
                minPrice != null ? BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? BigDecimal.valueOf(maxPrice) : null,
                condition, campus, sortBy, order);
    }

    @Override
    public Map<String, Object> getMyProducts(Integer pageNum, Integer pageSize) {
        return selectUserProductList(null, 1, pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getAdminProductList(Map<String, Object> params) {
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        String keyword = (String) params.get("keyword");
        int pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return selectAllProductList(status, keyword, pageNum, pageSize);
    }

    @Override
    public boolean adminDeleteProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(3);
        return baseMapper.updateById(product) > 0;
    }

    @Override
    public Map<String, Object> selectProductList(int pageNum, int pageSize, String keyword, Long categoryId,
                                                 BigDecimal minPrice, BigDecimal maxPrice, Integer condition, String campus, String sortBy, String order) {
        // 构建缓存key，只包含有效参数（过滤null）
        StringBuilder cacheKey = new StringBuilder(PRODUCT_LIST_PREFIX);
        cacheKey.append(pageNum).append(":").append(pageSize);
        if (keyword != null) cacheKey.append(":kw:").append(keyword);
        if (categoryId != null) cacheKey.append(":cat:").append(categoryId);
        if (minPrice != null) cacheKey.append(":min:").append(minPrice);
        if (maxPrice != null) cacheKey.append(":max:").append(maxPrice);
        if (condition != null) cacheKey.append(":cond:").append(condition);
        if (campus != null) cacheKey.append(":campus:").append(campus);
        if (sortBy != null) cacheKey.append(":sort:").append(sortBy);
        if (order != null) cacheKey.append(":order:").append(order);
        String key = cacheKey.toString();

        // 1. 先查 Redis 缓存
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedResult = (Map<String, Object>) redisTemplate.opsForValue().get(key);

        if (cachedResult != null) {
            return cachedResult;
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (campus != null && !campus.isEmpty()) {
            wrapper.eq(Product::getCampus, campus);
        }
        if (condition != null) {
            wrapper.eq(Product::getCondition, condition);
        }
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }
        // 排序字段白名单校验
        if (sortBy != null && !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ServiceException("非法的排序字段: " + sortBy);
        }
        if ("price".equals(sortBy)) {
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Product::getPrice);
            } else {
                wrapper.orderByDesc(Product::getPrice);
            }
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }
        int offset = (pageNum - 1) * pageSize;
        wrapper.last("limit " + offset + "," + pageSize);
        List<Product> list = baseMapper.selectList(wrapper);

        LambdaQueryWrapper<Product> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Product::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            countWrapper.like(Product::getTitle, keyword);
        }
        if (categoryId != null) {
            countWrapper.eq(Product::getCategoryId, categoryId);
        }
        if (campus != null && !campus.isEmpty()) {
            countWrapper.eq(Product::getCampus, campus);
        }
        if (condition != null) {
            countWrapper.eq(Product::getCondition, condition);
        }
        long total = baseMapper.selectCount(countWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        // 2. 存入 Redis，30分钟过期
        redisTemplate.opsForValue().set(key, result, 30, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public Product selectProductById(Long id, Long userId) {
        String key = PRODUCT_INFO_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached == null) {
            Product product = baseMapper.selectById(id);
            if (product != null) {
                redisTemplate.opsForValue().set(key, product, 1, TimeUnit.HOURS);
            } else {
                redisTemplate.opsForValue().set(key, "NULL", 5, TimeUnit.MINUTES);
            }
            return product;
        }

        if (cached instanceof Product) {
            return (Product) cached;
        }

        // 命中空值标记，防穿透
        return null;
    }

    @Override
    public Long publishProduct(Product product) {
        return baseMapper.insert(product) > 0 ? product.getId() : null;
    }

    @Override
    public Map<String, Object> selectUserProductList(Long userId, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Product::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        int offset = (pageNum - 1) * pageSize;
        wrapper.last("limit " + offset + "," + pageSize);
        List<Product> list = baseMapper.selectList(wrapper);

        LambdaQueryWrapper<Product> countWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            countWrapper.eq(Product::getUserId, userId);
        }
        if (status != null) {
            countWrapper.eq(Product::getStatus, status);
        }
        long total = baseMapper.selectCount(countWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean updateProductStatus(Long id, Integer status, Long userId) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        boolean result = baseMapper.updateById(product) > 0;
        if (result) {
            // 清除商品详情缓存
            String infoKey = PRODUCT_INFO_PREFIX + id;
            redisTemplate.delete(infoKey);
            // 列表缓存暂不主动清除，依赖 TTL 过期淘汰（避免 KEYS 命令阻塞 Redis）
        }
        return result;
    }

    // ==================== Set：用户收藏 ====================

    @Override
    public String toggleFavorite(Long productId, Long userId) {
        // 判断是否已收藏 → Set 的 SISMEMBER 操作，O(1)
        String key = USER_LIKES_PREFIX + userId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, productId.toString());

        if (Boolean.TRUE.equals(isMember)) {
            // 已收藏 → 取消收藏（SREM）
            redisTemplate.opsForSet().remove(key, productId.toString());
            return "已取消收藏";
        } else {
            // 未收藏 → 添加收藏（SADD）
            redisTemplate.opsForSet().add(key, productId.toString());
            return "已收藏";
        }
    }

    @Override
    public boolean isLiked(Long productId, Long userId) {
        // SISMEMBER user:likes:{userId} {productId} → 判断是否收藏
        String key = USER_LIKES_PREFIX + userId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, productId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    @Override
    public long getLikeCount(Long productId) {
        // 遍历所有用户的收藏 Set 不太现实，这里用 SCARD 拿到的是
        // 单个用户的收藏数。实际场景应维护 product:liked:count:{productId} 计数器
        // 这里只返回 -1 表示需要从数据库查，保留字段给后续优化
        return -1;
    }

    @Override
    public Set<String> getUserLikeIds(Long userId) {
        // SMEMBERS user:likes:{userId} → 返回用户收藏的全部商品 ID
        String key = USER_LIKES_PREFIX + userId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) return Collections.emptySet();
        return members.stream().map(Object::toString).collect(Collectors.toSet());
    }

    // ==================== ZSet：热门商品排行 ====================

    @Override
    public List<Product> getHotProducts(int topN) {
        // ZREVRANGE product:views:rank 0 topN-1 → 按浏览量降序取 Top N
        Set<Object> topIds = redisTemplate.opsForZSet()
                .reverseRange(PRODUCT_VIEWS_RANK, 0, topN - 1);
        if (topIds == null || topIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查缓存/数据库拿到完整 Product 对象
        List<Long> ids = topIds.stream()
                .map(o -> Long.valueOf(o.toString()))
                .collect(Collectors.toList());

        return ids.stream()
                .map(this::getProductById)  // 走缓存优先
                .collect(Collectors.toList());
    }

    // ==================== Hash：分类统计 ====================

    @Override
    public Map<Object, Object> getCategoryProductCounts() {
        // HGETALL product:category:stats → 返回全部分类及商品数量
        Map<Object, Object> stats = redisTemplate.opsForHash().entries(CATEGORY_STATS_KEY);
        // 按 value（商品数）降序排列
        return stats.entrySet().stream()
                .sorted((a, b) -> Long.compare(
                        Long.parseLong(b.getValue().toString()),
                        Long.parseLong(a.getValue().toString())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public boolean updateProductStatusForAdmin(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        return baseMapper.updateById(product) > 0;
    }

    @Override
    public Map<String, Object> selectAllProductList(Integer status, String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getTitle, keyword);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        int offset = (pageNum - 1) * pageSize;
        wrapper.last("limit " + offset + "," + pageSize);
        List<Product> list = baseMapper.selectList(wrapper);

        LambdaQueryWrapper<Product> countWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            countWrapper.eq(Product::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            countWrapper.like(Product::getTitle, keyword);
        }
        long total = baseMapper.selectCount(countWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public List<Product> selectAllForAdmin(Integer status, String keyword, int offset, int pageSize) {
        return baseMapper.selectAllForAdmin(status, keyword, offset, pageSize);
    }

    @Override
    public int selectAllCountForAdmin(Integer status, String keyword) {
        return baseMapper.selectAllCountForAdmin(status, keyword);
    }

    @Override
    public List<Product> aiRecommend(Long userId) {
        // AI智能推荐功能暂未实现，返回最新上架的商品列表作为默认推荐
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.orderByDesc(Product::getCreateTime);
        wrapper.last("limit 20");
        return baseMapper.selectList(wrapper);
    }
}
