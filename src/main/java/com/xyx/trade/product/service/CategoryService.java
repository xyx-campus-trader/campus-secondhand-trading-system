package com.xyx.trade.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyx.trade.product.domain.Category;
import com.xyx.trade.user.util.AjaxResult;

import java.util.List;

public interface CategoryService extends IService<Category> {
    /**
     * 获取所有分类
     */
    List<Category> getAllCategories();

    /**
     * 获取所有启用的分类
     */
    List<Category> getActiveCategories();

    /**
     * 根据父分类ID获取子分类
     */
    List<Category> getCategoriesByParentId(Long parentId);

    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Long id);

    /**
     * 添加分类
     */
    AjaxResult addCategory(Category category);

    /**
     * 更新分类
     */
    AjaxResult updateCategory(Category category);

    /**
     * 删除分类
     */
    AjaxResult deleteCategory(Long id);
}
