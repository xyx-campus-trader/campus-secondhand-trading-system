package com.xyx.trade.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyx.trade.user.domain.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM xyx_sys_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT id FROM xyx_sys_user WHERE student_id = #{studentId}")
    Long selectByStudentId(@Param("studentId") String studentId);

    @Select("SELECT * FROM xyx_sys_user WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    @Select("SELECT * FROM xyx_sys_user")
    List<User> selectAll();

    @Select("SELECT COUNT(*) FROM xyx_sys_user")
    int countUsers();

    @Update("UPDATE xyx_sys_user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
