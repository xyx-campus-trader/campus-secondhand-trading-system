package com.xyx.trade.banner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyx.trade.banner.domain.Banner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    @Select("SELECT * FROM xyx_banner WHERE status = 1 ORDER BY sort_order ASC")
    List<Banner> selectActiveBanners();

    @Select("SELECT * FROM xyx_banner ORDER BY sort_order ASC")
    List<Banner> selectAllBanners();

    @Insert("INSERT INTO xyx_banner(title, image_url, link_url, sort_order, status, start_time, end_time, create_time) " +
            "VALUES(#{title}, #{imageUrl}, #{linkUrl}, #{sortOrder}, #{status}, #{startTime}, #{endTime}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Banner banner);

    @Update("UPDATE xyx_banner SET title = #{title}, image_url = #{imageUrl}, link_url = #{linkUrl}, sort_order = #{sortOrder}, status = #{status}, start_time = #{startTime}, end_time = #{endTime} WHERE id = #{id}")
    int update(Banner banner);

    @Delete("DELETE FROM xyx_banner WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM xyx_banner WHERE id = #{id}")
    Banner selectById(@Param("id") Long id);
}
