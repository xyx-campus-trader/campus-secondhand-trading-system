## 修改Banner实体类id字段配置

### 问题分析

1. **Swagger UI不显示id字段**：当前Banner实体类的id字段被标记为`hidden = true`，导致Swagger UI不显示这个字段
2. **默认请求体缺少id字段**：由于id字段被隐藏，Swagger UI生成的默认请求体不包含id字段，不符合修改轮播图接口的要求
3. **API需求文档已更新**：已经在API需求文档中添加了正确的请求体示例，包含id字段

### 解决方案

修改Banner实体类的id字段配置：
- 将`@ApiModelProperty`注解中的`hidden`属性从`true`改为`false`，或者移除该属性
- 确保id字段在Swagger UI中可见，并且有正确的示例值
- 这样Swagger UI生成的默认请求体就会包含id字段

### 具体修改内容

* 修改`src/main/java/com/dnui/campussecondhand/banner/domain/Banner.java`文件：
  - 更新id字段的`@ApiModelProperty`注解，移除`hidden = true`属性
  - 确保id字段在Swagger UI中可见，并且有正确的示例值

### 预期效果

1. Swagger UI中修改轮播图接口的请求体包含id字段
2. 默认请求体符合API需求文档中的示例
3. 前端可以直接使用默认请求体进行测试，无需手动添加id字段
4. 所有轮播图相关接口都能正常工作

### 代码示例

```java
// 修改后的id字段配置
/**
 * ID
 */
@ApiModelProperty(value = "轮播图ID", example = "1")
private Long id;
```