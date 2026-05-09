package com.xyx.trade.user.interceptor;

import com.xyx.trade.user.domain.User;
import com.xyx.trade.user.service.UserService;
import com.xyx.trade.user.util.AjaxResult;
import com.xyx.trade.user.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 允许OPTIONS请求通过，用于处理跨域预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Authorization头
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(401);
            response.getWriter()
                    .write(new ObjectMapper().writeValueAsString(AjaxResult.error(401, "未授权访问: 缺少Authorization头")));
            return false;
        }

        // 提取token
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        // 验证 token
        boolean isValid = jwtUtils.validateToken(token);
        if (!isValid) {
            log.warn("JWT token无效或已过期");
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(401);
            response.getWriter().write(new ObjectMapper().writeValueAsString(AjaxResult.error(401, "token 无效或已过期")));
            return false;
        }

        // 从 token 中解析用户信息并校验状态
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtUtils.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.parseLong(claims.getSubject());
            String role = claims.get("role", String.class);
            String username = claims.get("username", String.class);

            // 从数据库查询用户信息，校验用户状态
            User user = userService.getUserById(userId);
            if (user == null) {
                log.warn("JWT验证失败：用户不存在，userId={}", userId);
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(401);
                response.getWriter().write(new ObjectMapper().writeValueAsString(AjaxResult.error(401, "用户不存在")));
                return false;
            }

            if (user.getStatus() == null || user.getStatus() != 1) {
                log.warn("JWT验证失败：账号已被禁用，userId={}, status={}", userId, user.getStatus());
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(403);
                response.getWriter().write(new ObjectMapper().writeValueAsString(AjaxResult.error(403, "账号已被禁用")));
                return false;
            }

            // 用用户名判断角色，确保管理员角色正确
            if ("admin".equals(username)) {
                role = "ADMIN";
            }

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            request.setAttribute("username", username);

            // 管理员接口权限拦截（双重保险）
            if (request.getRequestURI().startsWith("/api/admin")) {
                if (!"ADMIN".equals(role)) {
                    log.warn("管理员接口无权限访问 → userId={}, username={}, role={}, uri={}", userId, username, role, request.getRequestURI());
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(403);
                    response.getWriter().write(new ObjectMapper().writeValueAsString(AjaxResult.error(403, "无访问权限：仅管理员可访问")));
                    return false;
                }
            }

            log.info("JWT验证通过 → userId={}, username={}, role={}", userId, username, role);
        } catch (Exception e) {
            log.error("Token解析异常：{}", e.getMessage());
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(401);
            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(AjaxResult.error(401, "token 解析失败")));
            return false;
        }

        return true;
    }
}
