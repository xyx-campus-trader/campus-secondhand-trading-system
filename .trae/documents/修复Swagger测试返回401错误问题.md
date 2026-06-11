## 修复Swagger测试返回401错误问题

### 问题分析

1. **当前问题**：Swagger测试获取用户信息接口返回401错误，提示"未授权访问: 缺少Authorization头"，但Postman测试正常

2. **根本原因**：
   - Swagger的安全配置中，securityReferences的配置不正确
   - 虽然Swagger UI中输入了Authorization头，但在发送请求时，Authorization头没有正确传递到后端
   - 错误信息"未授权访问: 缺少Authorization头"说明JwtInterceptor没有收到Authorization头

3. **详细分析**：
   - UserController.java中的getUserInfo方法使用HttpServletRequest获取用户ID，ID由JwtInterceptor设置
   - JwtInterceptor.java正确实现了从Authorization头获取token的逻辑
   - WebMvcConfig.java正确配置了拦截路径
   - SwaggerConfig.java配置了安全方案，但securityReferences的配置可能有问题
   - 安全上下文配置中，AuthorizationScope数组为空，可能导致Authorization头不被正确传递

### 修复方案

1. **修改SwaggerConfig.java中的安全上下文配置**：
   - 修改securityContext方法，确保Authorization头能正确传递
   - 为securityReferences添加正确的AuthorizationScope
   - 确保Swagger UI能正确将Authorization头添加到请求中

2. **优化JwtInterceptor的日志输出**：
   - 添加更详细的日志，便于调试
   - 确保能看到完整的请求头信息

3. **验证接口路径**：
   - 确保接口路径配置正确
   - 确保拦截器配置与实际接口路径匹配

### 修改内容

1. **文件**：src/main/java/com/dnui/campussecondhand/config/SwaggerConfig.java
   - 修改securityContext方法，添加正确的AuthorizationScope
   - 确保securityReferences配置正确

2. **文件**：src/main/java/com/dnui/campussecondhand/user/interceptor/JwtInterceptor.java
   - 优化日志输出，添加更详细的调试信息

### 预期效果

* Swagger UI测试获取用户信息接口时，Authorization头能正确传递
* 返回200 OK状态码
* 正确返回用户信息
* 不再返回401错误

### 风险评估

* 不影响现有功能，只是优化Swagger配置
* 不需要修改业务逻辑
* 是Swagger的配置问题，不是代码逻辑问题

### 修复步骤

1. 修改SwaggerConfig.java，优化安全上下文配置
2. 优化JwtInterceptor的日志输出
3. 重启应用程序
4. 使用Swagger UI测试获取用户信息接口
5. 验证返回结果

### 技术细节

* Swagger的securityContext配置需要正确的securityReferences
* AuthorizationScope用于定义API的访问范围
* 缺少正确的AuthorizationScope可能导致Authorization头不被传递
* 优化日志输出有助于调试问题

### 预期结果

* Swagger UI测试获取用户信息接口成功
* 返回200 OK状态码
* 正确返回用户信息
* Postman测试仍然正常
* 解决了Swagger测试返回401错误的问题