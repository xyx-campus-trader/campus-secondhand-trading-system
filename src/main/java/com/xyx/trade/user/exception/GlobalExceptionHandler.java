package com.xyx.trade.user.exception;

import com.xyx.trade.user.util.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理HttpMessageNotReadableException异常
     * 当请求体参数格式错误时，例如日期格式不正确，会抛出该异常
     * 
     * @param e 异常对象
     * @return AjaxResult 统一的错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AjaxResult handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return AjaxResult.paramError("请求参数格式错误");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder("参数校验失败: ");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage()).append("; ");
        }
        return AjaxResult.paramError(sb.toString());
    }

    /**
     * 处理其他所有异常
     * 提供兜底的异常处理，确保所有异常都能返回统一的格式
     * 
     * @param e 异常对象
     * @return AjaxResult 统一的错误响应
     */
    /**
     * 处理业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e) {
        return AjaxResult.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        log.error("系统异常", e);
        return AjaxResult.error("服务器内部错误，请稍后重试");
    }
}
