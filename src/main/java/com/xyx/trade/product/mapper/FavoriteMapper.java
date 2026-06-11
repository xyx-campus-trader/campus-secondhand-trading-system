package com.xyx.trade.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyx.trade.product.domain.Favorite;
import com.xyx.trade.product.domain.FavoriteVO;
import com.xyx.trade.product.domain.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT * FROM xyx_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    Favorite selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Insert("INSERT INTO xyx_favorite(user_id, product_id, create_time) VALUES(#{userId}, #{productId}, NOW())")
    int insert(Favorite favorite);

    @Delete("DELETE FROM xyx_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT * FROM xyx_favorite WHERE user_id = #{userId}")
    List<Favorite> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM xyx_favorite WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM xyx_favorite")
    List<Favorite> selectAll();

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM xyx_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int checkIsFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Delete("DELETE FROM xyx_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    void deleteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT p.* FROM xyx_favorite f JOIN xyx_product p ON f.product_id = p.id WHERE f.user_id = #{userId} LIMIT #{offset}, #{pageSize}")
    List<Product> selectFavoriteProductsByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM xyx_favorite WHERE user_id = #{userId}")
    int selectTotalCountByUserId(@Param("userId") Long userId);

    @Select("SELECT f.id, u.nickname AS userNickname, p.title AS productTitle, DATE_FORMAT(f.create_time, '%Y-%m-%d %H:%i:%s') AS createTime FROM xyx_favorite f LEFT JOIN xyx_sys_user u ON f.user_id = u.id LEFT JOIN xyx_product p ON f.product_id = p.id ORDER BY f.create_time DESC")
    List<FavoriteVO> selectAllFavorites();

    @Select("SELECT f.id, f.user_id AS userId, f.product_id AS productId, DATE_FORMAT(f.create_time, '%Y-%m-%d %H:%i:%s') AS createTime, u.nickname AS userNickname, p.title AS productTitle, p.price AS productPrice FROM xyx_favorite f LEFT JOIN xyx_sys_user u ON f.user_id = u.id LEFT JOIN xyx_product p ON f.product_id = p.id ORDER BY f.create_time DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectAllFavoritesWithPagination(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM xyx_favorite")
    int selectAllCount();
}

