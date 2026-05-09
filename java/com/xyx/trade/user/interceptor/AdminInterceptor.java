package com.xyx.trade.user.interceptor;

import com.xyx.trade.user.util.AjaxResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从request属性中获取角色信息（由JwtInterceptor设置）
        String role = (String) request.getAttribute("role");

        if ("ADMIN".equals(role)) {
            return true;
        }

        // 无权访问
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(403);
        response.getWriter().write(new ObjectMapper().writeValueAsString(AjaxResult.error(403, "无权访问管理员功能")));
        return false;
    }
}

