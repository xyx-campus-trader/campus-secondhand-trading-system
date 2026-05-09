package com.xyx.trade.product.controller;

import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private ProductService productService;

    @GetMapping("/ai")
    public Map<String, Object> aiRecommend(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Product> list = productService.aiRecommend(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return Map.of("code", 200, "msg", "获取成功", "data", result);
    }
}
