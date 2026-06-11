## 修复登录接口Swagger参数显示问题

### 问题分析
1. **当前问题**：登录接口在Swagger UI中显示"No parameters"，但Request body中显示了完整的User对象
2. **期望效果**：只显示username和password两个登录必填参数
3. **根本原因**：
   - login方法直接使用User对象作为请求体参数
   - @ApiImplicitParam注解的dataTypeClass属性配置不正确
   - Swagger无法正确解析请求体参数

### 修复方案
1. **优化Swagger注解**：
   - 修改UserController.java中的login方法
   - 更新@ApiImplicitParam注解，添加dataTypeClass属性
   - 确保dataTypeClass属性正确指向User类

2. **增强接口文档**：
   - 为login方法添加更详细的@ApiOperation描述
   - 确保Swagger能正确识别请求体中的必填参数

### 修改内容
- **文件**：src/main/java/com/dnui/campussecondhand/user/controller/UserController.java
- **方法**：login方法
- **修改点**：
  - 更新@ApiImplicitParam注解，添加dataTypeClass属性
  - 确保Swagger能正确识别请求体参数

### 预期效果
- Swagger UI显示登录接口的username和password参数
- 不再显示完整的User对象
- 接口文档更清晰，便于测试和使用

### 风险评估
- 不影响现有功能，只是优化Swagger文档
- 不需要修改业务逻辑
- 不新增文件，只修改现有注解

### 修复步骤
1. 检查UserController.java中的login方法
2. 修改@ApiImplicitParam注解，添加dataTypeClass属性
3. 重启应用程序
4. 验证Swagger UI中登录接口的参数显示