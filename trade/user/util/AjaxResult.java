package com.xyx.trade.user.util;

import java.io.Serializable;

/**
 * Controller 层 Ajax 请求通用返回结果
 * 纯 JDK 原生实现，无外部依赖
 *
 * @param <T> 数据泛型，支持任意返回数据类型
 */
public class AjaxResult<T> implements Serializable {
    // 序列化版本号（保证序列化/反序列化兼容性）
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 200 - 操作成功
     * 500 - 系统内部异常
     * 400 - 请求参数错误
     * 401 - 未登录/登录过期
     * 403 - 权限不足
     * 404 - 请求资源不存在
     * 可根据业务扩展其他状态码
     */
    private int code;

    /**
     * 响应提示信息
     */
    private String msg;

    /**
     * 响应数据体
     */
    private T data;

    // 私有构造器（禁止外部直接实例化，统一通过静态方法创建）
    private AjaxResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ====================== 静态构建方法（语义化，简化使用） ======================

    /**
     * 通用成功返回（无数据，默认提示语）
     */
    public static <T> AjaxResult<T> success() {
        return new AjaxResult<>(200, "操作成功", null);
    }

    /**
     * 通用成功返回（带数据，默认提示语）
     */
    public static <T> AjaxResult<T> success(T data) {
        return new AjaxResult<>(200, "操作成功", data);
    }

    /**
     * 通用成功返回（自定义提示语 + 数据）
     */
    public static <T> AjaxResult<T> success(String msg, T data) {
        return new AjaxResult<>(200, msg, data);
    }

    /**
     * 通用失败返回（默认 500 状态码 + 自定义提示语）
     */
    public static <T> AjaxResult<T> error(String msg) {
        return new AjaxResult<>(500, msg, null);
    }

    /**
     * 通用失败返回（自定义状态码 + 提示语）
     */
    public static <T> AjaxResult<T> error(int code, String msg) {
        return new AjaxResult<>(code, msg, null);
    }

    /**
     * 参数错误返回（400 状态码）
     */
    public static <T> AjaxResult<T> paramError(String msg) {
        return new AjaxResult<>(400, msg, null);
    }

    /**
     * 未授权返回（401 状态码，如未登录/Token过期）
     */
    public static <T> AjaxResult<T> unauthorized(String msg) {
        return new AjaxResult<>(401, msg, null);
    }

    /**
     * 权限不足返回（403 状态码）
     */
    public static <T> AjaxResult<T> forbidden(String msg) {
        return new AjaxResult<>(403, msg, null);
    }

    /**
     * 资源不存在返回（404 状态码）
     */
    public static <T> AjaxResult<T> notFound(String msg) {
        return new AjaxResult<>(404, msg, null);
    }

    // ====================== Getter & Setter（必须实现，支持序列化/取值） ======================
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // ====================== toString（方便日志打印/调试） ======================
    @Override
    public String toString() {
        return "AjaxResult{" +
                "code=" + code +
                ", msg='" + msg + "'" +
                ", data=" + data +
                "}";
    }
}
