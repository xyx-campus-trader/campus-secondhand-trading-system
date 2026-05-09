package com.xyx.trade.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.mapper.ProductMapper;
import com.xyx.trade.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_INFO_PREFIX = "product:info:";
    private static final String PRODUCT_LIST_PREFIX = "product:list:";

    @Override
    public Long createProduct(Product product) {
        return baseMapper.insert(product) > 0 ? product.getId() : null;
    }

    @Override
    public boolean updateProduct(Product product) {
        boolean rows = baseMapper.updateById(product) > 0;
        if (rows) {
            // 更新成功 → 删除缓存
            String key = PRODUCT_INFO_PREFIX + product.getId();
            redisTemplate.delete(key);
        }
        return rows;
    }

    @Override
    public boolean deleteProduct(Long id) {
        boolean rows = baseMapper.deleteById(id) > 0;
        if (rows) {
            // 删除成功 → 删除缓存
            String key = PRODUCT_INFO_PREFIX + id;
            redisTemplate.delete(key);
        }
        return rows;
    }

    @Override
    public Product getProductById(Long id) {
        // 1. 先查 Redis
        String key = PRODUCT_INFO_PREFIX + id;
        Product product = (Product) redisTemplate.opsForValue().get(key);

        if (product == null) {
            // 2. Redis 没有，查数据库
            product = baseMapper.selectById(id);

            if (product != null) {
                // 3. 放入 Redis，过期时间 1 小时
                redisTemplate.opsForValue().set(key, product, 1, TimeUnit.HOURS);
            }
        }
        return product;
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
        if ("price".equals(sortBy)) {
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Product::getPrice);
            } else {
                wrapper.orderByDesc(Product::getPrice);
            }
        } else if ("createTime".equals(sortBy)) {
            wrapper.orderByDesc(Product::getCreateTime);
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
        // 1. 先查 Redis
        String key = PRODUCT_INFO_PREFIX + id;
        Product product = (Product) redisTemplate.opsForValue().get(key);

        if (product == null) {
            // 2. Redis 没有，查数据库
            product = baseMapper.selectById(id);

            if (product != null) {
                // 3. 放入 Redis，过期时间 1 小时
                redisTemplate.opsForValue().set(key, product, 1, TimeUnit.HOURS);
            }
        }
        return product;
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
            // 清除商品信息缓存
            String infoKey = PRODUCT_INFO_PREFIX + id;
            redisTemplate.delete(infoKey);
            // 清除商品列表缓存（前台列表）
            try {
                Set<String> listKeys = redisTemplate.keys(PRODUCT_LIST_PREFIX + "*");
                if (listKeys != null && !listKeys.isEmpty()) {
                    redisTemplate.delete(listKeys);
                }
                // 清除管理后台商品列表缓存（admin:product:list:*）
                Set<String> adminKeys = redisTemplate.keys("admin:product:list:*");
                if (adminKeys != null && !adminKeys.isEmpty()) {
                    redisTemplate.delete(adminKeys);
                }
            } catch (Exception e) {
                // 缓存清除失败不影响主业务
            }
        }
        return result;
    }

    @Override
    public String toggleFavorite(Long productId, Long userId) {
        return "功能暂未实现";
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
