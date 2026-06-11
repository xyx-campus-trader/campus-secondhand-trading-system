package com.xyx.trade.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyx.trade.user.domain.User;
import com.xyx.trade.user.util.AjaxResult;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     */
    AjaxResult register(User user);

    /**
     * 用户登录
     */
    AjaxResult login(String username, String password, String userType);

    /**
     * 获取当前用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 根据 ID 获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 更新用户信息
     */
    AjaxResult updateUserInfo(User user);

    /**
     * 查询所有用户（管理员）
     */
    List<User> getAllUsers();

    /**
     * 修改用户状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 修改密码
     */
    AjaxResult changePassword(Long userId, String oldPassword, String newPassword);



}

