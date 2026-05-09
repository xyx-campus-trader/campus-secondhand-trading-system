// XYX专属接口

const API_BASE_URL = '/api';
const USE_MOCK = false; // Rigidly set to false as per user request

// Token Management
const TokenManager = {
    setToken: (token) => localStorage.setItem('token', token),
    getToken: () => localStorage.getItem('token'),
    removeToken: () => localStorage.removeItem('token'),
    getUser: () => JSON.parse(localStorage.getItem('user') || 'null'),
    setUser: (user) => localStorage.setItem('user', JSON.stringify(user))
};

// Generic Fetch Wrapper
async function request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;

    // Default Headers
    const headers = {
        ...options.headers
    };

    // Only set Content-Type to application/json if not FormData, not GET request, and not already set
    if (!(options.body instanceof FormData) && (options.method || 'GET').toUpperCase() !== 'GET') {
        if (!headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }
    } else {
        // For FormData or GET requests, we don't need Content-Type
        delete headers['Content-Type'];
    }

    // Add Auth Token
    const token = TokenManager.getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Create config object
    const config = {
        ...options,
        headers
    };

    // Remove body for GET requests
    if ((options.method || 'GET').toUpperCase() === 'GET') {
        delete config.body;
    }

    try {
        const response = await fetch(url, config);

        // Handle 401 Unauthorized
        if (response.status === 401) {
            TokenManager.removeToken();
            window.location.href = './login.html';
            throw new Error('Unauthorized');
        }

        // Get response text first
        const text = await response.text();
        console.log('API Response:', text);
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);

        // Only parse JSON if response is ok
        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}: ${text}`);
        }

        // Try to parse JSON
        let resData;
        try {
            resData = JSON.parse(text);
        } catch (jsonError) {
            console.error('JSON parse error:', jsonError);
            console.error('Response text:', text);
            throw new Error(`Invalid JSON response: ${jsonError.message}`);
        }

        // Standard Response Handling
        if (resData.code !== 200) {
            // Some APIs might return non-200 for logic errors but still valid JSON
            throw new Error(resData.msg || 'Request failed');
        }

        return resData;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// API Endpoints Collection
const API = {
    // User
    login: (data) => request('/user/login', { method: 'POST', body: JSON.stringify(data) }),
    register: (data) => request('/user/register', { method: 'POST', body: JSON.stringify(data) }),
    getUserInfo: () => request('/user/getInfo', { method: 'GET' }),
    updateUser: (data) => request('/user/update', { method: 'PUT', body: JSON.stringify(data) }),
    getUserById: (id) => request(`/user/getById?id=${id}`, { method: 'GET' }),
    userLogout: () => request('/user/logout', { method: 'GET' }),

    // Product
    getProductList: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/product/list?${query}`, { method: 'GET' });
    },
    getProductDetail: (id) => request(`/product/detail/${id}`, { method: 'GET' }),
    publishProduct: (data) => request('/product/publish', { method: 'POST', body: JSON.stringify(data) }),
    getMyProducts: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/product/my-list?${query}`, { method: 'GET' });
    },
    updateProductStatus: (id, status) => request(`/product/status/${id}?status=${status}`, { method: 'PUT' }),

    // Recommend
    getAiRecommend: () => request('/recommend/ai', { method: 'GET' }),

    // AI Chat
    aiChat: (msg) => request(`/ai/chat?msg=${encodeURIComponent(msg)}`, { method: 'GET' }),
    aiSearchSuggest: (keyword) => request(`/ai/search/suggest?keyword=${encodeURIComponent(keyword)}`, { method: 'GET' }),

    // Favorites
    toggleFavorite: (id) => request(`/product/favorite/${id}`, { method: 'POST' }),
    getFavorites: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/product/favorite/list?${query}`, { method: 'GET' });
    },


    // Order
    createOrder: (data) => request('/order/create', { method: 'POST', body: JSON.stringify(data) }),
    getMyOrders: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/order/my-orders?${query}`, { method: 'GET' });
    },
    cancelOrder: (id, reason) => {
        const query = reason ? `?reason=${encodeURIComponent(reason)}` : '';
        return request(`/order/cancel/${id}${query}`, { method: 'POST' });
    },
    confirmReceipt: (id) => request(`/order/complete/${id}`, { method: 'PUT' }),
    payOrder: (id) => request(`/order/pay/${id}`, { method: 'POST' }),

    // Review
    createReview: (data) => request('/review/create', { method: 'POST', body: JSON.stringify(data) }),
    getReviews: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/review/list?${query}`, { method: 'GET' });
    },
    deleteReview: (id) => request(`/review/delete/${id}`, { method: 'DELETE' }),
    updateReview: (data) => request('/review/update', { method: 'PUT', body: JSON.stringify(data) }),

    // Banner
    getBanners: (count = 5) => request(`/banner/list?count=${count}`, { method: 'GET' }),
    addBanner: (data) => request('/banner/add', { method: 'POST', body: JSON.stringify(data) }),
    updateBanner: (data) => request('/banner/update', { method: 'PUT', body: JSON.stringify(data) }),
    deleteBanner: (id) => request(`/banner/delete/${id}`, { method: 'DELETE' }),

    // Admin
    adminGetUserList: (keyword) => {
        const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
        return request(`/admin/user/list${query}`, { method: 'GET' });
    },
    adminUpdateUserStatus: (id, status) => request(`/admin/user/status?id=${id}&status=${status}`, { method: 'PUT' }),
    adminDeleteUser: (id) => request(`/admin/user/${id}`, { method: 'DELETE' }),
    adminUpdateUser: (id, data) => request(`/admin/user/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    adminGetProductList: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/admin/product/list?${query}`, { method: 'GET' });
    },
    adminDeleteProduct: (id) => request(`/admin/product/${id}`, { method: 'DELETE' }),
    adminUpdateProduct: (id, data) => request(`/admin/product/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    adminGetOrderList: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/admin/order/list?${query}`, { method: 'GET' });
    },
    adminUpdateOrderStatus: (id, status) => request(`/admin/order/status?id=${id}&status=${status}`, { method: 'PUT' }),
    adminGetStats: () => request('/admin/stats'),
    adminGetBannerList: () => request('/admin/banner/list'),
    adminGetFavoriteList: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/admin/favorite/list?${query}`, { method: 'GET' });
    },
    adminDeleteFavorite: (id) => request(`/admin/favorite/${id}`, { method: 'DELETE' }),
    adminGetReviewList: (params) => {
        const query = new URLSearchParams(params).toString();
        return request(`/admin/review/list?${query}`, { method: 'GET' });
    },
    adminReplyReview: (id, adminReply) => request('/review/admin/reply', { method: 'POST', body: JSON.stringify({ id, adminReply }) }),


    // Common/File
    uploadFile: (file) => {
        const formData = new FormData();
        formData.append('file', file);
        return request('/common/upload', {
            method: 'POST',
            body: formData
        });
    },


    // Example of a mock function implementation if needed
    _mockLogin: async (data) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({
                code: 200,
                msg: "Login Success",
                data: {
                    token: "mock-token-123",
                    user: { id: 1, nickname: "Student User", avatarUrl: "assets/avatar.png" }
                }
            });
        }, 500));
    }
};

window.API = API;
window.TokenManager = TokenManager;
