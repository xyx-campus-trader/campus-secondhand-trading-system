package com.xyx.trade.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.product.domain.Category;
import com.xyx.trade.product.mapper.CategoryMapper;
import com.xyx.trade.product.service.CategoryService;
import com.xyx.trade.user.exception.ServiceException;
import com.xyx.trade.user.util.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectAll();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryMapper.selectActiveCategories();
    }

    @Override
    public List<Category> getCategoriesByParentId(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public AjaxResult addCategory(Category category) {
        try {
            // 验证分类名称
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                return AjaxResult.error("分类名称不能为空");
            }

            // 设置默认值
            if (category.getParentId() == null) {
                category.setParentId(0L);
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }
            if (category.getStatus() == null) {
                category.setStatus(1);
            }

            // 保存分类
            categoryMapper.insert(category);
            return AjaxResult.success("分类添加成功", category.getId());
        } catch (Exception e) {
            log.warn("分类添加失败", e);
            return AjaxResult.error("分类添加失败");
        }
    }

    @Override
    public AjaxResult updateCategory(Category category) {
        try {
            // 验证分类是否存在
            Category existingCategory = categoryMapper.selectById(category.getId());
            if (existingCategory == null) {
                return AjaxResult.error("分类不存在");
            }

            // 验证分类名称
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                return AjaxResult.error("分类名称不能为空");
            }

            // 更新分类
            categoryMapper.updateById(category);
            return AjaxResult.success("分类更新成功");
        } catch (Exception e) {
            log.warn("分类更新失败", e);
            return AjaxResult.error("分类更新失败");
        }
    }

    @Override
    public AjaxResult deleteCategory(Long id) {
        try {
            // 验证分类是否存在
            Category existingCategory = categoryMapper.selectById(id);
            if (existingCategory == null) {
                return AjaxResult.error("分类不存在");
            }

            // 检查是否有子分类
            List<Category> subCategories = categoryMapper.selectByParentId(id);
            if (!subCategories.isEmpty()) {
                return AjaxResult.error("该分类下有子分类，无法删除");
            }

            // 删除分类
            categoryMapper.deleteById(id);
            return AjaxResult.success("分类删除成功");
        } catch (Exception e) {
            log.warn("分类删除失败", e);
            return AjaxResult.error("分类删除失败");
        }
    }
}
