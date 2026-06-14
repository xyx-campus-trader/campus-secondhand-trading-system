package com.xyx.trade.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.user.domain.User;
import com.xyx.trade.user.mapper.UserMapper;
import com.xyx.trade.user.service.UserService;
import com.xyx.trade.user.util.AjaxResult;
import com.xyx.trade.user.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtils jwtUtils;

    // 密码加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    @Override
    public AjaxResult register(User user) {
        // 1. 检查用户名是否已存在
        User existingUser = baseMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            return AjaxResult.error("用户名已存在");
        }

        // 2. 检查学号是否已存在
        Long existingStudentId = baseMapper.selectByStudentId(user.getStudentId());
        if (existingStudentId != null) {
            return AjaxResult.error("学号已存在");
        }

        // 3. 【新增】手机号选填校验：如果填写了手机号，必须格式正确
        String phone = user.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            // 手机号不为空，校验格式
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return AjaxResult.error("手机号格式不正确");
            }
        } else {
            // 手机号为空，设置为 null
            user.setPhone(null);
        }

        // 4. 密码加密
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        // 5. 插入用户信息（使用 MP 的 insert 方法）
        baseMapper.insert(user);
        return AjaxResult.success("注册成功", user.getId());
    }

    /**
     * 用户登录
     */
    @Override
    public AjaxResult login(String username, String password, String userType) {
        // 1. 根据用户名查询用户
        User user = baseMapper.selectByUsername(username);
        if (user == null) {
            return AjaxResult.error("用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return AjaxResult.error("用户名或密码错误");
        }

        // 3. 【严格校验】用户状态：必须为 1 才允许登录，其他任何值都禁止登录
        Integer status = user.getStatus();
        System.out.println("=== Login Debug: username=" + username + ", status=" + status + " ===");
        if (status == null || status != 1) {
            System.out.println("=== Login Failed: Account disabled, status=" + status + " ===");
            return AjaxResult.error("账号已被禁用，请联系管理员");
        }

        // 4. 核心：校验「账号真实类型」和「选择的类型」是否匹配
        System.out.println("=== Login Debug: username=" + username + ", userType=" + userType + " ===");
        // 固定：用户名为 admin 就是管理员
        String realRole;
        if ("admin".equals(username)) {
            realRole = "ADMIN";
            if (!"ADMIN".equals(userType)) {
                return AjaxResult.error("管理员账号请选择【管理员】类型登录");
            }
        } else {
            realRole = "USER";
            if (!"USER".equals(userType)) {
                return AjaxResult.error("普通用户请选择【普通用户】类型登录");
            }
        }

        // 5. 生成 JWT 令牌
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), realRole);

        // 6. 构建返回结果
        System.out.println("=== Login Success: username=" + username + ", role=" + realRole + " ===");
        return AjaxResult.success("登录成功", new LoginResult(user.getId(), user.getNickname(), realRole, token));
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public User getUserInfo(Long userId) {
        return baseMapper.selectById(userId);
    }

    /**
     * 根据 ID 获取用户信息
     */
    @Override
    public User getUserById(Long userId) {
        return baseMapper.selectById(userId);
    }

    /**
     * 更新用户信息
     */
    @Override
    public AjaxResult updateUserInfo(User user) {
        try {
            // 1. 检查学号是否已被其他用户占用（排除当前用户）
            if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
                Long existingUserId = baseMapper.selectByStudentId(user.getStudentId());
                if (existingUserId != null && !existingUserId.equals(user.getId())) {
                    return AjaxResult.error("学号已存在");
                }
            }

            // 2. 【新增】手机号选填校验：如果填写了手机号，必须格式正确
            String phone = user.getPhone();
            if (phone != null && !phone.trim().isEmpty()) {
                // 手机号不为空，校验格式
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return AjaxResult.error("手机号格式不正确");
                }
            } else {
                // 手机号为空，设置为 null
                user.setPhone(null);
            }

            // 3. 更新用户信息（使用 MP 的 updateById 方法）
            baseMapper.updateById(user);
            return AjaxResult.success("修改成功");
        } catch (Exception e) {
            // 捕获唯一约束冲突异常
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry") && e.getMessage().contains("student_id")) {
                return AjaxResult.error("学号已存在");
            }
            // 其他异常
            log.warn("修改用户信息失败", e);
            return AjaxResult.error("修改失败");
        }
    }

    /**
     * 登录结果内部类
     */
    private static class LoginResult {
        private Long userId;
        private String nickname;
        private String role;
        private String token;

        public LoginResult(Long userId, String nickname, String role, String token) {
            this.userId = userId;
            this.nickname = nickname;
            this.role = role;
            this.token = token;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    @Override
    public java.util.List<User> getAllUsers() {
        java.util.List<User> users = baseMapper.selectAll();
        // 修正 admin 用户的角色
        for (User user : users) {
            if ("admin".equals(user.getUsername())) {
                user.setRole("ADMIN");
            }
        }
        return users;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return baseMapper.updateStatus(id, status) > 0;
    }

    @Override
    public AjaxResult changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 根据用户ID获取用户信息
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }

        // 2. 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return AjaxResult.error("原密码错误");
        }

        // 3. 对新密码进行加密
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);

        // 4. 更新密码
        try {
            baseMapper.updateById(user);
            return AjaxResult.success("密码修改成功");
        } catch (Exception e) {
            log.warn("密码修改失败", e);
            return AjaxResult.error("密码修改失败");
        }
    }
}

