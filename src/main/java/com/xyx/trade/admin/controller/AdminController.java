package com.xyx.trade.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Objects;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyx.trade.banner.domain.Banner;
import com.xyx.trade.banner.service.BannerService;
import com.xyx.trade.order.domain.Order;
import com.xyx.trade.order.service.OrderService;
import com.xyx.trade.product.domain.Favorite;
import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.service.FavoriteService;
import com.xyx.trade.product.service.ProductService;
import com.xyx.trade.review.domain.Review;
import com.xyx.trade.review.service.ReviewService;
import com.xyx.trade.user.service.UserService;
import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "管理员接口")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private com.xyx.trade.user.mapper.UserMapper userMapper;

    @Autowired
    private com.xyx.trade.product.mapper.ProductMapper productMapper;

    @Autowired
    private com.xyx.trade.order.mapper.OrderMapper orderMapper;

    @Autowired
    private com.xyx.trade.review.mapper.ReviewMapper reviewMapper;

    @Autowired
    private com.xyx.trade.product.mapper.FavoriteMapper favoriteMapper;

    @Autowired
    private com.xyx.trade.banner.mapper.BannerMapper bannerMapper;

    @ApiOperation("获取系统统计数据")
    @GetMapping("/stats")
    public AjaxResult<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userMapper.countUsers());
        stats.put("productCount", productMapper.countActiveProducts());
        stats.put("orderCount", orderMapper.selectAllCount(null, null));
        int reviewCount = reviewMapper.countAllReviews();
        stats.put("reviewCount", reviewCount);
        BigDecimal totalSales = orderMapper.sumTotalAmount();
        stats.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);
        return AjaxResult.success(stats);
    }

    @ApiOperation("获取今日统计数据")
    @GetMapping("/statistics")
    public AjaxResult<?> getStatistics() {
        int todayOrderCount = orderService.countTodayOrder();
        BigDecimal todayAmount = orderService.sumTodayAmount();
        Map<String, Object> map = new HashMap<>();
        map.put("todayOrderCount", todayOrderCount);
        map.put("todayAmount", todayAmount != null ? todayAmount : BigDecimal.ZERO);
        return AjaxResult.success(map);
    }

    @ApiOperation("查询所有用户（支持搜索）")
    @GetMapping("/user/list")
    public AjaxResult<?> getUserList(
            @RequestParam(required = false) String keyword) {
        java.util.List<com.xyx.trade.user.domain.User> users = userService.getAllUsers();
        if (keyword != null && !keyword.trim().isEmpty()) {
            java.util.List<com.xyx.trade.user.domain.User> filteredUsers = new java.util.ArrayList<>();
            for (com.xyx.trade.user.domain.User user : users) {
                if ((user.getUsername() != null && user.getUsername().contains(keyword)) ||
                    (user.getNickname() != null && user.getNickname().contains(keyword)) ||
                    (user.getPhone() != null && user.getPhone().contains(keyword))) {
                    filteredUsers.add(user);
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("list", filteredUsers);
            data.put("total", filteredUsers.size());
            return AjaxResult.success(data);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", users);
        data.put("total", users.size());
        return AjaxResult.success(data);
    }

    @ApiOperation("修改用户状态")
    @PutMapping("/user/status")
    public AjaxResult<?> updateUserStatus(@RequestParam Long id, @RequestParam Integer status) {
        if (userService.updateStatus(id, status)) {
            return AjaxResult.success("状态更新成功");
        }
        return AjaxResult.error("状态更新失败");
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/user/{id}")
    public AjaxResult<?> deleteUser(@PathVariable Long id) {
        try {
            userService.removeById(id);
            return AjaxResult.success("用户删除成功");
        } catch (Exception e) {
            return AjaxResult.error("用户删除失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/user/{id}")
    public AjaxResult<?> getUserInfo(@PathVariable Long id) {
        try {
            com.xyx.trade.user.domain.User user = userService.getUserById(id);
            if (user == null) {
                return AjaxResult.error("用户不存在");
            }
            // 修正 admin 用户的角色
            if ("admin".equals(user.getUsername())) {
                user.setRole("ADMIN");
            }
            return AjaxResult.success(user);
        } catch (Exception e) {
            return AjaxResult.error("获取用户信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/user/{id}")
    public AjaxResult<?> updateUser(@PathVariable Long id, @RequestBody com.xyx.trade.user.domain.User user) {
        try {
            // 确保路径参数和请求体中的ID一致
            if (!id.equals(user.getId())) {
                return AjaxResult.error("路径参数和请求体中的ID不一致");
            }
            
            // 检查用户名是否已被其他用户占用（排除当前用户）
            com.xyx.trade.user.domain.User existingUser = userMapper.selectByUsername(user.getUsername());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                return AjaxResult.error("用户名已存在");
            }
            
            // 检查学号是否已被其他用户占用（排除当前用户）
            if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
                Long existingUserId = userMapper.selectByStudentId(user.getStudentId());
                if (existingUserId != null && !existingUserId.equals(user.getId())) {
                    return AjaxResult.error("学号已存在");
                }
            }
            
            // 手机号选填校验：如果填写了手机号，必须格式正确
            String phone = user.getPhone();
            if (phone != null && !phone.trim().isEmpty()) {
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return AjaxResult.error("手机号格式不正确");
                }
            } else {
                user.setPhone(null);
            }
            
            userService.updateById(user);
            return AjaxResult.success("更新成功");
        } catch (Exception e) {
            return AjaxResult.error("更新失败：" + e.getMessage());
        }
    }

    @ApiOperation("添加用户")
    @PostMapping("/user/add")
    public AjaxResult<?> addUser(@RequestBody com.xyx.trade.user.domain.User user) {
        try {
            // 检查用户名是否已存在
            com.xyx.trade.user.domain.User existingUser = userMapper.selectByUsername(user.getUsername());
            if (existingUser != null) {
                return AjaxResult.error("用户名已存在");
            }
            
            // 检查学号是否已存在
            if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
                Long existingUserId = userMapper.selectByStudentId(user.getStudentId());
                if (existingUserId != null) {
                    return AjaxResult.error("学号已存在");
                }
            }
            
            // 手机号选填校验：如果填写了手机号，必须格式正确
            String phone = user.getPhone();
            if (phone != null && !phone.trim().isEmpty()) {
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return AjaxResult.error("手机号格式不正确");
                }
            } else {
                user.setPhone(null);
            }
            
            // 密码加密
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置默认状态
            user.setStatus(1);
            
            // 保存用户
            userService.save(user);
            return AjaxResult.success("添加成功");
        } catch (Exception e) {
            return AjaxResult.error("添加失败：" + e.getMessage());
        }
    }

    @ApiOperation("查询所有商品（支持搜索）")
    @GetMapping("/product/list")
    public AjaxResult<?> getProductList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        
        // 生成缓存 key
        String key = "admin:product:list:" + status + ":" + keyword + ":" + pageNum + ":" + pageSize;
        
        // 1. 先查 Redis
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        if (cachedData != null) {
            return AjaxResult.success(cachedData);
        }

        int offset = (pageNum - 1) * pageSize;
        List<Product> productList = productService.selectAllForAdmin(status, keyword, offset, pageSize);
        int total = productService.selectAllCountForAdmin(status, keyword);
        
        // 批量查询卖家信息
        List<Long> sellerIds = productList.stream()
            .map(Product::getUserId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        Map<Long, com.xyx.trade.user.domain.User> userMap = new HashMap<>();
        if (!sellerIds.isEmpty()) {
            List<com.xyx.trade.user.domain.User> users = userService.getAllUsers();
            for (com.xyx.trade.user.domain.User user : users) {
                userMap.put(user.getId(), user);
            }
        }
        
        // 为每个商品设置卖家昵称
        final Map<Long, com.xyx.trade.user.domain.User> finalUserMap = userMap;
        productList.forEach(product -> {
            com.xyx.trade.user.domain.User seller = finalUserMap.get(product.getUserId());
            product.setSellerNickname(seller != null ? seller.getNickname() : "未知用户");
        });
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", productList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        // 2. 放入 Redis，过期时间 30 分钟
        redisTemplate.opsForValue().set(key, data, 30, TimeUnit.MINUTES);
        
        return AjaxResult.success(data);
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/product/{id}")
    public AjaxResult<?> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return AjaxResult.success("商品删除成功");
        }
        return AjaxResult.error("商品删除失败");
    }

    @ApiOperation("更新商品状态（上架/下架）")
    @PutMapping("/product/status")
    public AjaxResult<?> updateProductStatus(@RequestParam Long id, @RequestParam Integer status) {
        if (productService.updateProductStatusForAdmin(id, status)) {
            return AjaxResult.success("状态更新成功");
        }
        return AjaxResult.error("状态更新失败");
    }

    @ApiOperation("更新商品信息")
    @PutMapping("/product/{id}")
    public AjaxResult<?> updateProduct(@PathVariable Long id, @RequestBody java.util.Map<String, Object> updateData) {
        try {
            // 先获取现有商品
            com.xyx.trade.product.domain.Product product = productService.getProductById(id);
            if (product == null) {
                return AjaxResult.error("商品不存在");
            }
            
            // 只更新提供的字段
            if (updateData.containsKey("title")) {
                product.setTitle((String) updateData.get("title"));
            }
            if (updateData.containsKey("price")) {
                Object price = updateData.get("price");
                if (price instanceof Number) {
                    product.setPrice(new java.math.BigDecimal(price.toString()));
                }
            }
            if (updateData.containsKey("description")) {
                product.setDescription((String) updateData.get("description"));
            }
            if (updateData.containsKey("categoryId")) {
                Object catId = updateData.get("categoryId");
                if (catId instanceof Number) {
                    product.setCategoryId(((Number) catId).longValue());
                }
            }
            if (updateData.containsKey("status")) {
                Object status = updateData.get("status");
                if (status instanceof Number) {
                    product.setStatus(((Number) status).intValue());
                }
            }
            
            productService.updateProduct(product);
            
            // 清除商品列表缓存
            try {
                java.util.Set<String> keys = redisTemplate.keys("admin:product:list:*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception cacheEx) {
                // 缓存清除失败不影响主业务
            }
            
            return AjaxResult.success("商品更新成功");
        } catch (Exception e) {
            return AjaxResult.error("商品更新失败：" + e.getMessage());
        }
    }

    @ApiOperation("查询所有订单（支持搜索）")
    @GetMapping("/order/list")
    public AjaxResult<?> getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        
        int offset = (pageNum - 1) * pageSize;
        List<Order> orderList = orderMapper.selectAll(status, keyword, offset, pageSize);
        int total = orderMapper.selectAllCount(status, keyword);
        
        // 批量查询用户和商品信息
        List<Long> buyerIds = orderList.stream().map(Order::getBuyerId).distinct().collect(java.util.stream.Collectors.toList());
        List<Long> sellerIds = orderList.stream().map(Order::getSellerId).distinct().collect(java.util.stream.Collectors.toList());
        List<Long> productIds = orderList.stream().map(Order::getProductId).distinct().collect(java.util.stream.Collectors.toList());
        
        Map<Long, com.xyx.trade.user.domain.User> userMap = new HashMap<>();
        if (!buyerIds.isEmpty() || !sellerIds.isEmpty()) {
            List<com.xyx.trade.user.domain.User> users = userService.getAllUsers();
            for (com.xyx.trade.user.domain.User user : users) {
                userMap.put(user.getId(), user);
            }
        }
        
        Map<Long, com.xyx.trade.product.domain.Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (Long productId : productIds) {
                com.xyx.trade.product.domain.Product product = productService.getProductById(productId);
                if (product != null) {
                    productMap.put(productId, product);
                }
            }
        }
        
        // 为每个订单设置买家/卖家昵称和商品信息
        orderList.forEach(order -> {
            com.xyx.trade.user.domain.User buyer = userMap.get(order.getBuyerId());
            com.xyx.trade.user.domain.User seller = userMap.get(order.getSellerId());
            com.xyx.trade.product.domain.Product product = productMap.get(order.getProductId());
            
            order.setBuyerNickname(buyer != null ? buyer.getNickname() : "未知买家");
            order.setSellerNickname(seller != null ? seller.getNickname() : "未知卖家");
            order.setProductTitle(product != null ? product.getTitle() : "未知商品");
            order.setPrice(product != null ? product.getPrice() : java.math.BigDecimal.ZERO);
        });
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", orderList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        return AjaxResult.success(data);
    }

    @ApiOperation("更新订单状态")
    @PutMapping("/order/status")
    public AjaxResult<?> updateOrderStatus(@RequestParam Long id, @RequestParam Integer status) {
        if (orderService.updateOrderStatus(id, status)) {
            return AjaxResult.success("订单状态更新成功");
        }
        return AjaxResult.error("订单状态更新失败");
    }

    @ApiOperation("查询所有评价")
    @GetMapping("/review/list")
    public AjaxResult<?> getReviewList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        int offset = (pageNum - 1) * pageSize;
        List<Review> reviewList = reviewMapper.selectAdminList(null, null, offset, pageSize);
        int total = reviewMapper.selectAdminCount(null, null);
        
        // 批量查询用户信息，获取评论人昵称
        List<Long> reviewerIds = reviewList.stream()
                .map(Review::getReviewerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        Map<Long, com.xyx.trade.user.domain.User> userMap = new HashMap<>();
        if (!reviewerIds.isEmpty()) {
            List<com.xyx.trade.user.domain.User> users = userService.getAllUsers();
            for (com.xyx.trade.user.domain.User user : users) {
                userMap.put(user.getId(), user);
            }
        }
        
        // 为每个评论设置评论人昵称
        reviewList.forEach(review -> {
            com.xyx.trade.user.domain.User reviewer = userMap.get(review.getReviewerId());
            if (reviewer != null) {
                review.setReviewerNickname(reviewer.getNickname());
            }
        });
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", reviewList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        return AjaxResult.success(data);
    }

    @ApiOperation("查询所有轮播图")
    @GetMapping("/banner/list")
    public AjaxResult<?> getAdminBannerList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order");
        
        Page<Banner> page = new Page<>(pageNum, pageSize);
        Page<Banner> resultPage = bannerMapper.selectPage(page, queryWrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", resultPage.getRecords());
        data.put("total", resultPage.getTotal());
        data.put("pageNum", resultPage.getCurrent());
        data.put("pageSize", resultPage.getSize());
        
        return AjaxResult.success(data);
    }

    @ApiOperation("查询所有收藏记录")
    @GetMapping("/favorite/list")
    public AjaxResult<?> getAdminFavoriteList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            // 使用专门的管理员查询方法，支持分页和关联查询
            Map<String, Object> result = favoriteService.getAllFavorites(pageNum, pageSize);
            return AjaxResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取收藏列表失败：" + e.getMessage());
        }
    }

    @ApiOperation("删除收藏记录")
    @DeleteMapping("/favorite/{id}")
    public AjaxResult<?> deleteFavorite(@PathVariable Long id) {
        try {
            favoriteService.removeById(id);
            return AjaxResult.success("收藏删除成功");
        } catch (Exception e) {
            return AjaxResult.error("收藏删除失败：" + e.getMessage());
        }
    }
}
