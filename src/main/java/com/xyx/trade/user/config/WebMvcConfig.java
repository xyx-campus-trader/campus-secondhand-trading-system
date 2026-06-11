package com.xyx.trade.user.config;

import com.xyx.trade.user.interceptor.AdminInterceptor;
import com.xyx.trade.user.interceptor.JwtInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * WebMvc配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
        @Autowired
        private JwtInterceptor jwtInterceptor;

        @Autowired
        private AdminInterceptor adminInterceptor;

        /**
         * 解决前端 long 类型精度丢失问题
         * 将 Long 类型序列化为 String
         */
        /**
         * 配置消息转换器
         * 主要解决前后端交互时 Long 类型精度丢失的问题。
         * JS 的 Number 类型精度只有 16 位，而 Java 的 Long 类型有 19 位。
         * 通过自定义序列化器，将所有 Long 类型自动转换为 String 类型传输给前端。
         */
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
                MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleModule simpleModule = new SimpleModule();
                // 将 Long 类型序列化为 String
                simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
                objectMapper.registerModule(simpleModule);
                jackson2HttpMessageConverter.setObjectMapper(objectMapper);
                converters.add(0, jackson2HttpMessageConverter);
        }

        /**
         * 注册拦截器
         * 配置拦截器的执行顺序和拦截路径。
         * 1. jwtInterceptor: 负责身份认证，解析 Token。
         * 2. adminInterceptor: 负责权限控制，校验是否为管理员。
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(jwtInterceptor)
                                // 需要拦截的路径 (需要登录才能访问)
                                .addPathPatterns(
                                                "/api/user/getInfo",
                                                "/api/user/update",
                                                "/api/product/publish",
                                                "/api/product/my-list",
                                                "/api/product/status/**",
                                                "/api/banner/**",
                                                "/api/product/favorite/**",
                                                "/api/order/create",
                                                "/api/order/my-orders",
                                                "/api/order/cancel/**",
                                                "/api/order/pay/**",
                                                "/api/order/complete/**",
                                                "/api/admin/**",
                                                "/api/review/create",
                                                "/api/review/update",
                                                "/api/review/delete/**",
                                                "/api/review/admin/**")
                                // 不需要拦截的路径 (即白名单，无需登录即可访问)
                                .excludePathPatterns(
                                                "/api/user/register",
                                                "/api/user/login",
                                                "/api/user/getById",
                                                "/api/banner/list",
                                                "/api/banner/detail/**",
                                                "/api/product/list",
                                                "/api/product/detail/**");

                // 管理员拦截器，必须在 JwtInterceptor 之后执行
                // 只拦截后台管理相关接口
                registry.addInterceptor(adminInterceptor)
                                .addPathPatterns("/api/admin/**", "/api/review/admin/**");
        }

        /**
         * 配置跨域资源共享 (CORS)
         * 允许前端不同域名的请求访问后端接口。
         * 解决前后端分离部署时产生的跨域问题。
         */
        @Override
        public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                registry.addMapping("/**") // 允许所有的路径
                                .allowedOriginPatterns("*") // 允许所有的源
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                                .allowedHeaders("*") // 允许所有的请求头
                                .allowCredentials(true) // 允许携带 Cookie
                                .maxAge(3600); // 预检请求的有效期
        }
}

