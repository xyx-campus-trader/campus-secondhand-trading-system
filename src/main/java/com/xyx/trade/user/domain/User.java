package com.xyx.trade.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 用户实体类
 * 对应数据库表：xyx_sys_user
 */
@ApiModel(description = "用户信息实体")
@TableName("xyx_sys_user")
public class User {
    /**
     * 用户 ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "用户 ID", example = "1")
    private Long id;

    /**
     * 用户名（唯一）
     */
    @ApiModelProperty(value = "用户名", example = "admin", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在 2-20 个字符之间")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "用户密码", example = "123456", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 个字符之间")
    private String password;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "用户昵称", example = "管理员", required = true)
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    /**
     * 学号（唯一）
     */
    @ApiModelProperty(value = "学号", example = "20210101")
    @Size(max = 20, message = "学号长度不能超过 20 个字符")
    private String studentId;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号码", example = "13800138000")
    @Size(max = 11, message = "手机号长度不能超过 11 位")
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱地址", example = "admin@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 性别（0:未知，1:男，2:女）
     */
    @ApiModelProperty(value = "性别", example = "0", notes = "0:未知，1:男，2:女")
    private Integer gender;

    /**
     * 校区
     */
    @ApiModelProperty(value = "校区", example = "主校区")
    private String campus;

    /**
     * 宿舍楼
     */
    @ApiModelProperty(value = "宿舍楼", example = "1 号楼")
    private String dormitory;

    /**
     * 头像 URL
     */
    @ApiModelProperty(value = "头像 URL", example = "http://example.com/avatar.jpg")
    private String avatarUrl;

    /**
     * 信用分
     */
    @ApiModelProperty(value = "信用分", example = "100")
    private Integer creditScore;

    /**
     * 状态（0:禁用，1:正常）
     */
    @ApiModelProperty(value = "用户状态", example = "1", notes = "0:禁用，1:正常")
    private Integer status;

    /**
     * 角色（USER,ADMIN）
     */
    @ApiModelProperty(value = "用户角色", example = "USER", notes = "USER,ADMIN")
    private String role;

    /**
     * JWT 令牌
     */
    @ApiModelProperty(value = "JWT 令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    // 无参构造器
    public User() {
        // 默认信用分 100，默认性别未知，默认状态正常，默认角色 USER
        this.creditScore = 100;
        this.gender = 0;
        this.status = 1;
        this.role = "USER";
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getDormitory() {
        return dormitory;
    }

    public void setDormitory(String dormitory) {
        this.dormitory = dormitory;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

