## 修复Banner实体类和Mapper与建表语句匹配问题

### 问题分析

1. **表名不匹配**：Mapper中使用的表名是`sys_banner`，而建表语句中是`banner`
2. **字段不匹配**：Mapper中使用了不存在的`update_time`字段，而建表语句中没有这个字段
3. **实体类与Mapper不匹配**：实体类中没有`updateTime`字段，但Mapper的`selectList`方法查询了该字段

### 解决方案

1. **修改BannerMapper.java**：
   - 将所有SQL语句中的表名从`sys_banner`改为`banner`
   - 移除所有涉及`update_time`字段的查询和操作
   - 在`selectList`方法中添加缺失的字段（status, startTime, endTime）

2. **实体类无需修改**：
   - 当前Banner实体类的字段已经与建表语句匹配
   - 字段名称和类型都正确对应

### 具体修改内容

* 修改`selectList`方法：
  - 表名从`sys_banner`改为`banner`
  - 移除`update_time as updateTime`字段
  - 添加`status, start_time as startTime, end_time as endTime`字段

* 修改`selectById`方法：
  - 表名从`sys_banner`改为`banner`

* 修改`insert`方法：
  - 表名从`sys_banner`改为`banner`
  - 移除`update_time`字段的插入

* 修改`update`方法：
  - 表名从`sys_banner`改为`banner`
  - 移除`update_time`字段的更新
  - 添加对`status, startTime, endTime`字段的更新支持

* 修改`deleteById`方法：
  - 表名从`sys_banner`改为`banner`

### 预期效果

1. Mapper中的SQL语句与建表语句完全匹配
2. 实体类与Mapper完全匹配
3. `/api/banner/detail/{id}`接口不再返回500错误
4. 所有轮播图相关接口都能正常工作

### 代码示例

```java
// 修改后的selectList方法
@Select("SELECT id, title, image_url as imageUrl, link_url as linkUrl, sort_order as sortOrder, status, start_time as startTime, end_time as endTime, create_time as createTime FROM banner ORDER BY sort_order ASC LIMIT #{count}")
List<Banner> selectList(int count);
```