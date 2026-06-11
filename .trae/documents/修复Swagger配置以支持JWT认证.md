## 修复Swagger配置以支持JWT认证

### 问题分析
1. 当前`SwaggerConfig.java`第67行存在语法错误
2. 缺少`securityContexts`配置，导致Swagger UI无法自动添加Authorization头
3. 安全配置不完整，无法实现全局Token支持

### 解决方案
修改`src/main/java/com/dnui/campussecondhand/config/SwaggerConfig.java`文件，按照Swagger 3.0规范添加完整的安全配置：

1. **修复语法错误**：删除第67行末尾多余的点
2. **添加`securityContexts`配置**：指定哪些接口需要携带Token
3. **添加`defaultAuth`方法**：配置默认的安全引用
4. **更新`userApi`方法**：添加`securityContexts`配置

### 具体修改内容

- 修复第67行的语法错误
- 添加`securityContexts`方法，配置全局Token支持
- 添加`defaultAuth`方法，定义安全引用
- 更新`userApi`方法，同时配置`securitySchemes`和`securityContexts`

### 预期效果
1. 重启项目后，Swagger UI页面上方会出现"Authorize"按钮
2. 点击按钮输入JWT Token后，所有请求都会自动携带Authorization头
3. `/api/user/getInfo`接口测试时不再返回401错误

### 代码示例
```java
// 完整的SwaggerConfig.java配置
@Configuration
@EnableOpenApi
@ConditionalOnProperty(name = "springfox.documentation.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfig {
    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("用户管理模块")
                .apiInfo(userApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.dnui.campussecondhand.user.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(apiKeyScheme()))
                .securityContexts(Collections.singletonList(securityContext()));
    }
    
    // 其他方法...
    
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.any())
                .build();
    }
    
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(
                new SecurityReference("Authorization", authorizationScopes));
    }
}
```