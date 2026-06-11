# 校园二手交易平台前端实施指南
本文档为**Vue3 + Vite**技术栈专属实施规范，用于指导前端开发者基于后端API快速搭建校园二手交易平台，统一开发标准、接口调用逻辑与页面实现方案。

## 一、 项目技术栈（固定选型，禁止随意修改）
| 分类 | 技术选型 | 核心用途 |
| :--- | :--- | :--- |
| 开发框架 | Vue 3 + Vite | 项目主体开发、构建 |
| UI组件库 | Element Plus | 页面组件、表单、弹窗、布局 |
| 路由管理 | Vue Router 4 | 页面跳转、路由守卫、权限控制 |
| 状态管理 | Pinia | 用户信息、全局状态、购物车/收藏 |
| 请求工具 | Axios | 接口请求、拦截器、统一异常处理 |
| 样式工具 | SCSS + 原生CSS | 页面样式定制、主题适配 |
| 工具库 | Day.js | 时间格式化、日期处理 |

## 二、 项目基础配置
### 1. 环境变量配置
新建 `.env.development` 开发环境配置文件，统一管理接口地址：
```env
# 后端接口基础地址
VITE_API_BASE_URL = http://localhost:8082
# 请求超时时间
VITE_API_TIMEOUT = 10000
```

### 2. Axios 封装（标准可用版）
创建 `src/utils/request.js`，**所有接口必须通过该实例调用**：
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: import.meta.env.VITE_API_TIMEOUT
})

// 请求拦截器：自动携带Token
service.interceptors.request.use(
  config => {
    // 从本地存储获取Token
    const token = localStorage.getItem('token')
    if (token) {
      // 后端要求格式：Bearer + 空格 + Token
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理返回结果与异常
service.interceptors.response.use(
  response => {
    const res = response.data
    // 后端成功状态码固定为200
    if (res.code === 200) {
      return res.data // 直接返回业务数据，简化页面调用
    }

    // 401：Token过期/未登录，强制跳转到登录页
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.clear() // 清空本地缓存
      router.push('/login')
    } else {
      // 其他业务异常，直接提示错误信息
      ElMessage.error(res.msg || '操作失败')
    }
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  error => {
    ElMessage.error('网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default service
```

## 三、 路由规划（标准路由表）
创建 `src/router/index.js`，包含**公共路由、权限路由**，路由守卫已内置登录校验：
```javascript
import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  // 公共页面（无需登录）
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情' } },

  // 买家中心（必须登录）
  { path: '/buyer/orders', name: 'BuyerOrders', component: () => import('@/views/buyer/Orders.vue'), meta: { requiresAuth: true, title: '我的订单' } },
  { path: '/buyer/favorites', name: 'BuyerFavorites', component: () => import('@/views/buyer/Favorites.vue'), meta: { requiresAuth: true, title: '我的收藏' } },
  { path: '/buyer/review/:orderId', name: 'BuyerReview', component: () => import('@/views/buyer/Review.vue'), meta: { requiresAuth: true, title: '发表评价' } },

  // 卖家中心（必须登录）
  { path: '/seller/publish', name: 'SellerPublish', component: () => import('@/views/seller/Publish.vue'), meta: { requiresAuth: true, title: '发布商品' } },
  { path: '/seller/products', name: 'SellerProducts', component: () => import('@/views/seller/ProductList.vue'), meta: { requiresAuth: true, title: '商品管理' } },

  // 个人中心（必须登录）
  { path: '/user/profile', name: 'UserProfile', component: () => import('@/views/user/Profile.vue'), meta: { requiresAuth: true, title: '个人资料' } },
  { path: '/user/reviews', name: 'UserReviews', component: () => import('@/views/user/Reviews.vue'), meta: { requiresAuth: true, title: '评价管理' } },

  // 404页面
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫：登录权限控制
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 需要登录的页面，未登录则跳转到登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')
    ElMessage.warning('请先登录')
  } else {
    next()
  }
})

export default router
```

## 四、 API接口统一管理
建议按模块拆分接口文件，示例：`src/api/user.js`、`src/api/product.js`、`src/api/order.js`

### 核心接口示例（直接复制使用）
```javascript
// 1. 用户模块 api/user.js
import request from '@/utils/request'
// 登录
export const userLogin = (data) => request.post('/api/user/login', data)
// 注册
export const userRegister = (data) => request.post('/api/user/register', data)
// 获取个人信息
export const getUserInfo = () => request.get('/api/user/getInfo')
// 修改个人信息
export const updateUserInfo = (data) => request.put('/api/user/update', data)

// 2. 商品模块 api/product.js
import request from '@/utils/request'
// 首页商品列表
export const getProductList = (params) => request.get('/api/product/list', { params })
// 商品详情
export const getProductDetail = (id) => request.get(`/api/product/detail/${id}`)
// 发布商品
export const publishProduct = (data) => request.post('/api/product/publish', data)
// 我的商品（卖家）
export const getMyProductList = () => request.get('/api/product/my-list')
// 修改商品状态
export const updateProductStatus = (id, status) => request.put(`/api/product/status/${id}`, { status })
// 收藏商品
export const addFavorite = (productId) => request.post('/api/product/favorite/add', { productId })
// 我的收藏
export const getFavoriteList = () => request.get('/api/product/favorite/list')

// 3. 订单模块 api/order.js
import request from '@/utils/request'
// 创建订单
export const createOrder = (data) => request.post('/api/order/create', data)
// 我的订单
export const getMyOrders = () => request.get('/api/order/my-orders')
// 确认收货
export const completeOrder = (id) => request.post(`/api/order/complete/${id}`)

// 4. 评价模块 api/review.js
import request from '@/utils/request'
// 发布评价
export const createReview = (data) => request.post('/api/review/create', data)
// 我的评价
export const getReviewList = () => request.get('/api/review/list')
```

## 五、 核心业务实现规范
### 1. 登录/注册逻辑
1. 调用登录接口 → 成功后**将token、用户信息存入localStorage**
2. 登录成功后跳转到首页
3. 退出登录：清空localStorage，跳转到登录页

### 2. 图片上传规范
- 后端接收：**URL字符串数组** 格式
- 开发阶段：直接输入网络图片URL，手动拼接为 `["url1","url2"]`
- 正式方案：对接阿里云OSS/七牛云，上传后获取URL，自动组装数组

### 3. 订单状态映射（前端固定配置）
```javascript
// 订单状态对应文案与样式
export const orderStatusMap = {
  0: { text: '待付款', type: 'danger' },
  1: { text: '待发货', type: 'warning' },
  2: { text: '待收货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'default' }
}
```

### 4. 商品新旧程度映射
```javascript
export const conditionMap = {
  0: '全新',
  1: '几乎全新',
  2: '轻微使用',
  3: '明显使用'
}
```

### 5. 模拟支付实现
后端无真实支付接口，前端直接实现：
1. 点击支付按钮 → 弹出确认框
2. 确认后提示「支付成功」，前端手动刷新订单状态
3. 状态流转：待付款 → 待发货

## 六、 页面开发规范
1. **文件夹结构**
```
src/
├── views/           # 页面文件夹
│   ├── buyer/       # 买家中心
│   ├── seller/      # 卖家中心
│   ├── user/        # 个人中心
│   ├── Home.vue     # 首页
│   ├── Login.vue    # 登录页
│   └── ...
├── components/      # 公共组件（商品卡片、导航栏等）
├── api/             # 接口请求
├── utils/           # 工具函数
├── store/           # Pinia状态管理
└── router/          # 路由
```

2. **组件命名**：大驼峰格式，如 `ProductCard.vue`
3. **样式规范**：使用 `scoped` 隔离样式，避免全局污染
4. **状态管理**：用户信息、全局配置存入Pinia

## 七、 调试与测试标准
1. 接口调试：优先使用 **Swagger** `http://localhost:8082/swagger-ui/index.html`
2. 前端调试：打开浏览器F12 → Network面板，检查：
   - 请求URL是否正确
   - 请求头是否携带 `Authorization` Token
   - 响应数据是否符合预期
3. 异常处理：所有接口请求必须添加 `try/catch`，避免页面崩溃

## 八、 核心数据结构参考（精简版）
### 1. 登录返回值
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "字符串",
    "userId": 1,
    "nickname": "张三",
    "avatarUrl": "图片地址"
  }
}
```

### 2. 商品列表项
```json
{
  "id": 1,
  "title": "商品标题",
  "price": 100.00,
  "coverImage": "封面图",
  "condition": 1,
  "campus": "校区",
  "user": { "nickname": "卖家昵称" }
}
```

### 3. 订单列表项
```json
{
  "id": 1,
  "orderNo": "订单号",
  "status": 0,
  "totalAmount": 100.00,
  "product": { "title": "商品名", "coverImage": "封面图" }
}
```

---

### 总结
1. 本指南**固定使用Vue3 + Vite + Element Plus**，所有配置可直接复制到项目中
2. Axios已完成Token拦截、异常处理、登录过期自动跳转，无需二次修改
3. 路由、接口、状态映射全部标准化，直接按照文档开发即可
4. 核心业务（登录、商品、订单、评价）逻辑已明确，降低开发沟通成本