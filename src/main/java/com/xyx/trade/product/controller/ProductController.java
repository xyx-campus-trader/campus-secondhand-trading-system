package com.xyx.trade.product.controller;

import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.service.ProductService;
import com.xyx.trade.user.exception.ServiceException;
import com.xyx.trade.user.service.BrowseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 商品控制器
 */
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private BrowseService browseService;

    /**
     * 发布商品
     */
    @ApiOperation("发布商品")
    @PostMapping("/create")
    public Map<String, Object> createProduct(@RequestBody Product product, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Map.of("code", 401, "msg", "请先登录");
            }
            product.setUserId(userId);
            product.setStatus(1);
            product.setCreateTime(new Date());
            product.setUpdateTime(new Date());
            Long id = productService.createProduct(product);
            return Map.of("code", 200, "msg", "发布成功", "data", id);
        } catch (ServiceException e) {
            return Map.of("code", 400, "msg", e.getMessage());
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }

    /**
     * 发布商品（前端请求路径）
     */
    @ApiOperation("发布商品")
    @PostMapping("/publish")
    public Map<String, Object> publishProduct(@RequestBody Product product, HttpServletRequest request) {
        try {
            // 从request中获取用户ID（由JwtInterceptor验证并设置）
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                log.warn("发布商品失败：用户未登录");
                return Map.of("code", 401, "msg", "请先登录");
            }
            
            // 参数校验
            if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
                return Map.of("code", 400, "msg", "商品标题不能为空");
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return Map.of("code", 400, "msg", "商品价格必须大于0");
            }
            if (product.getCoverImage() == null || product.getCoverImage().trim().isEmpty()) {
                return Map.of("code", 400, "msg", "商品封面图不能为空");
            }
            
            // 设置发布者ID，上架状态和创建时间
            product.setUserId(userId);
            product.setStatus(1); // 上架
            product.setCreateTime(new Date());
            product.setUpdateTime(new Date());
            
            log.info("用户{}发布商品：{}，价格：{}", userId, product.getTitle(), product.getPrice());
            
            Long id = productService.createProduct(product);
            log.info("商品发布成功，ID：{}", id);
            return Map.of("code", 200, "msg", "发布成功", "data", id);
        } catch (ServiceException e) {
            log.warn("发布商品业务异常：{}", e.getMessage());
            return Map.of("code", 400, "msg", e.getMessage());
        } catch (Exception e) {
            log.error("发布商品系统异常：", e);
            return Map.of("code", 500, "msg", "系统错误：" + e.getMessage());
        }
    }

    /**
     * 编辑商品
     */
    @ApiOperation("编辑商品")
    @PostMapping("/update")
    public Map<String, Object> updateProduct(@RequestBody Product product, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Map.of("code", 401, "msg", "请先登录");
            }
            Product existing = productService.getProductById(product.getId());
            if (existing == null) {
                return Map.of("code", 404, "msg", "商品不存在");
            }
            if (!existing.getUserId().equals(userId)) {
                return Map.of("code", 403, "msg", "无权操作该商品");
            }
            boolean success = productService.updateProduct(product);
            return Map.of("code", 200, "msg", "更新成功", "data", success);
        } catch (ServiceException e) {
            return Map.of("code", 400, "msg", e.getMessage());
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }

    /**
     * 删除商品
     */
    @ApiOperation("删除商品")
    @PostMapping("/delete")
    public Map<String, Object> deleteProduct(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Map.of("code", 401, "msg", "请先登录");
            }
            Long id = params.get("id");
            Product existing = productService.getProductById(id);
            if (existing == null) {
                return Map.of("code", 404, "msg", "商品不存在");
            }
            if (!existing.getUserId().equals(userId)) {
                return Map.of("code", 403, "msg", "无权操作该商品");
            }
            boolean success = productService.deleteProduct(id);
            return Map.of("code", 200, "msg", "删除成功", "data", success);
        } catch (ServiceException e) {
            return Map.of("code", 400, "msg", e.getMessage());
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }

    /**
     * 获取商品详情
     */
    @ApiOperation("获取商品详情")
    @GetMapping("/detail/{id}")
    public Map<String, Object> getProductDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId != null) {
                browseService.addBrowse(userId, id);
            }
            Product product = productService.getProductById(id);
            return Map.of("code", 200, "msg", "获取成功", "data", product);
        } catch (ServiceException e) {
            return Map.of("code", 400, "msg", e.getMessage());
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }

    /**
     * 获取商品列表
     */
    @ApiOperation("获取商品列表")
    @GetMapping("/list")
    public Map<String, Object> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) Integer condition,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        try {
            Map<String, Object> result = productService.getProductList(
                    categoryId, keyword, campus, condition, minPrice, maxPrice, sortBy, order, pageNum, pageSize);
            return Map.of("code", 200, "msg", "获取成功", "data", result);
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }

    /**
     * 获取我的商品
     */
    @ApiOperation("获取我的商品")
    @GetMapping("/my")
    public Map<String, Object> getMyProducts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Map.of("code", 401, "msg", "请先登录");
            }
            Map<String, Object> result = productService.selectUserProductList(userId, null, pageNum, pageSize);
            return Map.of("code", 200, "msg", "获取成功", "data", result);
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }

    /**
     * 获取我的商品列表
     */
    @ApiOperation("获取我的商品列表")
    @GetMapping("/my-list")
    public Map<String, Object> getMyProductList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        try {
            // 从request中获取用户ID（由JwtInterceptor验证并设置）
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Map.of("code", 401, "msg", "请先登录");
            }
            
            // 查询当前用户发布的商品
            Map<String, Object> result = productService.selectUserProductList(userId, null, pageNum, pageSize);
            return Map.of("code", 200, "msg", "获取成功", "data", result);
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }

    /**
     * 更新商品状态
     */
    @ApiOperation("更新商品状态")
    @PutMapping("/status/{productId}")
    public Map<String, Object> updateProductStatus(
            HttpServletRequest request,
            @PathVariable Long productId,
            @RequestParam Integer status) {
        try {
            // 从request中获取用户ID（由JwtInterceptor验证并设置）
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Map.of("code", 401, "msg", "请先登录");
            }

            // 校验商品是否属于当前用户，防止越权操作
            Product product = productService.getProductById(productId);
            if (product == null) {
                return Map.of("code", 404, "msg", "商品不存在");
            }
            if (!product.getUserId().equals(userId)) {
                return Map.of("code", 403, "msg", "无权操作该商品");
            }

            // 更新商品状态（0=下架，1=上架）
            boolean success = productService.updateProductStatus(productId, status, userId);

            if (success) {
                return Map.of("code", 200, "msg", "操作成功");
            } else {
                return Map.of("code", 500, "msg", "操作失败");
            }
        } catch (Exception e) {
            return Map.of("code", 500, "msg", "系统错误");
        }
    }
}

