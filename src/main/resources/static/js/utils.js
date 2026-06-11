/**
 * Utility Functions
 */

const Utils = {
    // Toast Notification
    showToast: (message, type = 'info') => {
        const container = document.querySelector('.toast-container') || Utils.createToastContainer();

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `<span>${message}</span>`;

        container.appendChild(toast);

        // Remove after 3 seconds
        setTimeout(() => {
            toast.style.animation = 'slideIn 0.3s ease reverse forwards';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    },

    createToastContainer: () => {
        const container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
        return container;
    },

    // URL Query Params
    getQueryParam: (param) => {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(param);
    },

    // Format Price
    formatPrice: (price) => {
        return `¥${Number(price).toFixed(2)}`;
    },

    // Backend Base URL (without /api)
    BACKEND_URL: 'http://localhost:8082',

    formatDate: (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    },

    // Get Full Image URL
    getImgUrl: (path, defaultImg = 'images/product_placeholder.png') => {
        if (!path) return defaultImg;
        if (path.startsWith('http')) return path;
        if (path.startsWith('/uploads/')) return Utils.BACKEND_URL + path;
        return path;
    },

    // DOM Helper: Check Login Status and Update UI
    checkAuth: async () => {
        const user = TokenManager.getUser();
        const authLinks = document.getElementById('auth-links');
        const userMenu = document.getElementById('user-menu');

        if (!authLinks || !userMenu) return;

        if (user) {
            authLinks.classList.add('hidden');
            userMenu.classList.remove('hidden');

            // 从后端获取最新用户信息，确保昵称显示正确
            try {
                const res = await API.getUserInfo();
                if (res.code === 200 && res.data) {
                    // 更新本地存储的用户信息
                    const latestUser = res.data;
                    TokenManager.setUser(latestUser);
                    
                    // Update Avatar if exists
                    const avatar = userMenu.querySelector('img');
                    if (avatar) {
                        avatar.src = Utils.getImgUrl(latestUser.avatarUrl, 'images/logo.png');
                    }

                    // Update Name - 使用后端返回的真实昵称
                    const nameSpan = userMenu.querySelector('.username');
                    if (nameSpan) nameSpan.textContent = latestUser.nickname || latestUser.username || '用户';
                }
            } catch (e) {
                console.warn('获取用户信息失败，使用缓存数据:', e);
                // 降级使用本地缓存
                const avatar = userMenu.querySelector('img');
                if (avatar) {
                    avatar.src = Utils.getImgUrl(user.avatarUrl, 'images/logo.png');
                }
                const nameSpan = userMenu.querySelector('.username');
                if (nameSpan) nameSpan.textContent = user.nickname || '用户';
            }

            // 根据用户角色控制管理后台链接显示
            Utils.checkAdminRole();
        } else {
            authLinks.classList.remove('hidden');
            userMenu.classList.add('hidden');
        }
    },

    // 检查用户角色，控制管理后台链接显示
    checkAdminRole: async () => {
        // 兼容多种 id 命名：导航栏和页脚
        const adminLink = document.getElementById('adminLink') || document.getElementById('admin-link');
        const footerAdminLink = document.getElementById('footer-admin-link');
        if (!adminLink && !footerAdminLink) return;

        try {
            // 调用 getInfo 接口获取最新用户信息
            const res = await API.getUserInfo();
            if (res.code === 200 && res.data) {
                const role = res.data.role;
                const isAdmin = role === 'ADMIN';
                
                // 控制导航栏管理后台链接
                if (adminLink) {
                    if (isAdmin) {
                        adminLink.classList.remove('hidden');
                    } else {
                        adminLink.classList.add('hidden');
                    }
                }
                
                // 控制页脚管理后台链接
                if (footerAdminLink) {
                    if (isAdmin) {
                        footerAdminLink.classList.remove('hidden');
                    } else {
                        footerAdminLink.classList.add('hidden');
                    }
                }
            }
        } catch (e) {
            console.warn('获取用户角色信息失败:', e);
            if (adminLink) adminLink.classList.add('hidden');
            if (footerAdminLink) footerAdminLink.classList.add('hidden');
        }
    },

    logout: async () => {
        // 二次确认，避免误操作
        if (!confirm('确定要退出登录吗？')) return;
        
        try {
            // 清除本地存储的token/用户信息（核心操作）
            TokenManager.removeToken();
            TokenManager.setUser(null);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            sessionStorage.clear();
            
            // 尝试调用后端退出登录接口（失败也不影响前端退出）
            try {
                await API.userLogout();
            } catch (apiErr) {
                console.log('后端退出接口调用失败，不影响前端退出');
            }
            
            // 跳转到登录页
            window.location.href = './login.html';
        } catch (err) {
            console.error('退出登录失败:', err);
            // 即使出错也要尝试跳转到登录页
            window.location.href = './login.html';
        }
    }
};

window.Utils = Utils;
