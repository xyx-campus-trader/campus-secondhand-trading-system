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

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
        @Autowired
        private JwtInterceptor jwtInterceptor;

        @Autowired
        private AdminInterceptor adminInterceptor;

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
                MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleModule simpleModule = new SimpleModule();
                simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
                objectMapper.registerModule(simpleModule);
                jackson2HttpMessageConverter.setObjectMapper(objectMapper);
                converters.add(0, jackson2HttpMessageConverter);
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(jwtInterceptor)
                                .addPathPatterns(
                                                "/api/user/getInfo",
                                                "/api/user/update",
                                                "/api/product/publish",
                                                "/api/product/create",
                                                "/api/product/update",
                                                "/api/product/delete",
                                                "/api/product/my",
                                                "/api/product/my-list",
                                                "/api/product/status/**",
                                                "/api/product/favorite/**",
                                                "/api/banner/**",
                                                "/api/category/add",
                                                "/api/category/update",
                                                "/api/category/delete",
                                                "/api/common/upload",
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
                                .excludePathPatterns(
                                                "/api/user/register",
                                                "/api/user/login",
                                                "/api/user/getById",
                                                "/api/banner/list",
                                                "/api/banner/detail/**",
                                                "/api/product/list",
                                                "/api/product/detail/**");

                registry.addInterceptor(adminInterceptor)
                                .addPathPatterns(
                                                "/api/admin/**",
                                                "/api/review/admin/**",
                                                "/api/banner/add",
                                                "/api/banner/update",
                                                "/api/banner/delete/**");
        }

        @Override
        public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                registry.addMapping("/**")
                                .allowedOriginPatterns("*")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*")
                                .allowCredentials(true)
                                .maxAge(3600);
        }
}
