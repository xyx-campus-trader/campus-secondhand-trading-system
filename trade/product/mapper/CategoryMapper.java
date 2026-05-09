package com.xyx.trade.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyx.trade.product.domain.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM xyx_category WHERE id = #{id}")
    Category selectById(Long id);

    @Select("SELECT * FROM xyx_category ORDER BY sort_order ASC, id ASC")
    List<Category> selectAll();

    @Insert("INSERT INTO xyx_category(name, parent_id, sort_order, status, icon, create_time) " +
            "VALUES(#{name}, #{parentId}, #{sortOrder}, #{status}, #{icon}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE xyx_category SET name = #{name}, parent_id = #{parentId}, sort_order = #{sortOrder}, " +
            "status = #{status}, icon = #{icon} WHERE id = #{id}")
    int updateById(Category category);

    @Delete("DELETE FROM xyx_category WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM xyx_category WHERE parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<Category> selectByParentId(Long parentId);

    @Select("SELECT * FROM xyx_category WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Category> selectActiveCategories();
}
