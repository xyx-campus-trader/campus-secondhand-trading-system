package com.xyx.trade.product.controller;

import com.xyx.trade.product.domain.Category;
import com.xyx.trade.product.service.CategoryService;
import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "分类管理")
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation(value = "获取所有分类", notes = "获取系统中所有商品分类")
    @GetMapping("/list")
    public AjaxResult getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return AjaxResult.success(categories);
    }

    @ApiOperation(value = "获取启用的分类", notes = "获取所有状态为启用的商品分类")
    @GetMapping("/active")
    public AjaxResult getActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        return AjaxResult.success(categories);
    }

    @ApiOperation(value = "根据父分类ID获取子分类", notes = "根据父分类ID获取子分类列表")
    @ApiParam(name = "parentId", value = "父分类ID，0表示一级分类", required = true, example = "0")
    @GetMapping("/children")
    public AjaxResult getCategoriesByParentId(@RequestParam Long parentId) {
        List<Category> categories = categoryService.getCategoriesByParentId(parentId);
        return AjaxResult.success(categories);
    }

    @ApiOperation(value = "根据ID获取分类", notes = "根据分类ID获取分类详情")
    @ApiParam(name = "id", value = "分类ID", required = true, example = "1")
    @GetMapping("/getById")
    public AjaxResult getCategoryById(@RequestParam Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            return AjaxResult.success(category);
        } else {
            return AjaxResult.error("分类不存在");
        }
    }

    @ApiOperation(value = "添加分类", notes = "添加新的商品分类（需要管理员权限）")
    @ApiParam(name = "category", value = "分类信息", required = true)
    @PostMapping("/add")
    public AjaxResult addCategory(@Valid @RequestBody Category category, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return AjaxResult.error(403, "无操作权限");
        }
        return categoryService.addCategory(category);
    }

    @ApiOperation(value = "更新分类", notes = "更新商品分类信息（需要管理员权限）")
    @ApiParam(name = "category", value = "分类信息", required = true)
    @PutMapping("/update")
    public AjaxResult updateCategory(@Valid @RequestBody Category category, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return AjaxResult.error(403, "无操作权限");
        }
        return categoryService.updateCategory(category);
    }

    @ApiOperation(value = "删除分类", notes = "删除商品分类（需要管理员权限）")
    @ApiParam(name = "id", value = "分类ID", required = true, example = "1")
    @DeleteMapping("/delete")
    public AjaxResult deleteCategory(@RequestParam Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return AjaxResult.error(403, "无操作权限");
        }
        return categoryService.deleteCategory(id);
    }

    private boolean isAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return "ADMIN".equals(role);
    }
}
