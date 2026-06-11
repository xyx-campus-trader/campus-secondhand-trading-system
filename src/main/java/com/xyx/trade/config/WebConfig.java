package com.xyx.trade.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * WebMvc配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加安全响应头
     * 解决浏览器控制台警告：Response should include 'x-content-type-options' header
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    public FilterRegistrationBean<Filter> securityHeadersFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new Filter() {
            @Override
            public void init(FilterConfig filterConfig) {}

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                // 防止 MIME 类型嗅探
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                // 防止点击劫持
                httpResponse.setHeader("X-Frame-Options", "DENY");
                // XSS 防护
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
                chain.doFilter(request, response);
            }

            @Override
            public void destroy() {}
        });
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    /**
     * 配置静态资源映射
     * 解决本地文件（如上传的图片）无法直接通过 URL 访问的问题。
     * 将虚拟路径 /uploads/** 映射到本地磁盘的 uploads 目录。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录下的 uploads 文件夹路径
        // System.getProperty("user.dir") 获取当前工作目录，确保在不同环境下路径正确
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // 注册静态资源处理器，将 /uploads/** 映射到本地磁盘路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        // 同时也支持基础资源 (static 目录下的 CSS, JS 等)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}

