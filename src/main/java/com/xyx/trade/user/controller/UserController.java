package com.xyx.trade.user.controller;

import com.xyx.trade.user.domain.User;
import com.xyx.trade.user.service.UserService;
import com.xyx.trade.user.util.AjaxResult;
import com.xyx.trade.user.util.JwtUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户Controller
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @ApiOperation(value = "用户注册", notes = "新用户注册账户")
    @ApiParam(name = "registerRequest", value = "注册请求对象", required = true)
    @PostMapping("/register")
    public AjaxResult register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setNickname(registerRequest.getNickname());
        user.setStudentId(registerRequest.getStudentId());
        user.setCampus(registerRequest.getCampus());
        user.setPhone(registerRequest.getPhone());
        return userService.register(user);
    }

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求对象，包含用户名、密码和用户类型
     * @return AjaxResult 登录结果，包含token
     */
    @ApiOperation(value = "用户登录", notes = "用户登录接口，返回JWT令牌")
    @ApiParam(name = "loginRequest", value = "登录请求对象", required = true)
    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.getUserType());
    }

    /**
     * 获取当前用户信息
     * 
     * @return AjaxResult 当前用户信息
     */
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前登录用户的详细信息")
    @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header", example = "Bearer eyJhbGciOiJIUzUxMiJ9...")
    @GetMapping("/getInfo")
    public AjaxResult getUserInfo(HttpServletRequest request) {
        // 从request中获取用户ID和角色（由JwtInterceptor验证并设置）
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        User user = userService.getUserInfo(userId);
        // 覆盖数据库中的角色，使用Token中的角色
        if (role != null) {
            user.setRole(role);
        }
        return AjaxResult.success(user);
    }

    /**
     * 根据ID获取用户信息（公开接口）
     * 
     * @param id 用户ID
     * @return AjaxResult 用户信息
     */
    @ApiOperation(value = "根据ID获取用户信息", notes = "根据ID获取用户详细信息，无需Token")
    @ApiParam(name = "id", value = "用户ID", required = true)
    @GetMapping("/getById")
    public AjaxResult getUserById(@RequestParam Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            // 敏感信息脱敏处理
            user.setPassword(null);
            return AjaxResult.success(user);
        } else {
            return AjaxResult.error("用户不存在");
        }
    }

    /**
     * 更新用户信息
     * 
     * @param authorization 请求头中的Authorization token
     * @param updateRequest 更新用户信息请求对象
     * @return AjaxResult 更新结果
     */
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的信息")
    @ApiParam(name = "updateRequest", value = "更新用户信息请求对象", required = true)
    @PutMapping("/update")
    public AjaxResult updateUserInfo(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdateUserRequest updateRequest) {
        // 从token中获取用户ID
        String token = authorization.substring(7); // 去掉"Bearer "前缀
        Long userId = jwtUtils.getUserIdFromToken(token);

        User user = new User();
        user.setId(userId);
        user.setNickname(updateRequest.getNickname());
        user.setPhone(updateRequest.getPhone());
        user.setAvatarUrl(updateRequest.getAvatarUrl());
        user.setCampus(updateRequest.getCampus());
        user.setStudentId(updateRequest.getStudentId());

        return userService.updateUserInfo(user);
    }

    /**
     * 更新用户信息请求对象
     */
    @ApiModel(description = "更新用户信息请求对象")
    public static class UpdateUserRequest {
        @Size(max = 20, message = "昵称长度不能超过20个字符")
        @ApiModelProperty(value = "昵称", example = "张三", required = false)
        private String nickname;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @ApiModelProperty(value = "手机号", example = "13800138000", required = false)
        private String phone;

        @ApiModelProperty(value = "头像URL", example = "http://example.com/avatar.jpg", required = false)
        private String avatarUrl;

        @ApiModelProperty(value = "校区", example = "东校区", required = false)
        private String campus;

        @ApiModelProperty(value = "学号", example = "20210001", required = false)
        private String studentId;

        // Getters and setters
        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getCampus() {
            return campus;
        }

        public void setCampus(String campus) {
            this.campus = campus;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }
    }

    /**
     * 注册请求对象
     */
    @ApiModel(description = "注册请求对象")
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 20, message = "用户名长度必须在 4-20 个字符之间")
        @ApiModelProperty(value = "用户名", example = "zhangsan", required = true)
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在 6-20 个字符之间")
        @ApiModelProperty(value = "密码", example = "123456", required = true)
        private String password;

        @NotBlank(message = "昵称不能为空")
        @ApiModelProperty(value = "昵称", example = "张三", required = true)
        private String nickname;

        @NotBlank(message = "学号不能为空")
        @ApiModelProperty(value = "学号", example = "20210001", required = true)
        private String studentId;

        @NotBlank(message = "校区不能为空")
        @ApiModelProperty(value = "校区", example = "东校区", required = true)
        private String campus;

        @ApiModelProperty(value = "手机号", example = "13800138000", required = false)
        private String phone;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getCampus() {
            return campus;
        }

        public void setCampus(String campus) {
            this.campus = campus;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     * 登录请求对象
     */
    @ApiModel(description = "登录请求对象")
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        @ApiModelProperty(value = "用户名", example = "zhangsan", required = true)
        private String username;

        @NotBlank(message = "密码不能为空")
        @ApiModelProperty(value = "密码", example = "123456", required = true)
        private String password;

        @NotBlank(message = "用户类型不能为空")
        @ApiModelProperty(value = "用户类型", example = "USER", required = true)
        private String userType;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }
    }

    /**
     * 密码修改请求对象
     */
    @ApiModel(description = "密码修改请求对象")
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        @ApiModelProperty(value = "原密码", example = "123456", required = true)
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "新密码长度必须在 6-20 个字符之间")
        @ApiModelProperty(value = "新密码", example = "12345678", required = true)
        private String newPassword;

        // Getters and setters
        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    @ApiOperation(value = "修改密码", notes = "修改当前登录用户的密码")
    @ApiParam(name = "changePasswordRequest", value = "密码修改请求对象", required = true)
    @PostMapping("/changePassword")
    public AjaxResult changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        // 从token中获取用户ID
        String token = authorization.substring(7); // 去掉"Bearer "前缀
        Long userId = jwtUtils.getUserIdFromToken(token);

        return userService.changePassword(userId, changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录接口")
    @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header", example = "Bearer eyJhbGciOiJIUzUxMiJ9...")
    @GetMapping("/logout")
    public AjaxResult logout() {
        // 清除本地存储的token/用户信息由前端完成
        // 这里可以添加其他退出登录逻辑，比如清除redis中的token等
        return AjaxResult.success("退出登录成功");
    }
}

