-- 创建数据库
CREATE DATABASE IF NOT EXISTS `campus_secondhand`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `campus_secondhand`;

-- 1. 用户表（sys_user）
CREATE TABLE `sys_user` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名（登录账号）',
                            `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密存储）',
                            `nickname` varchar(50) NOT NULL COMMENT '昵称',
                            `student_id` varchar(20) DEFAULT NULL COMMENT '学号',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
                            `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
                            `gender` tinyint(1) DEFAULT '0' COMMENT '性别（0:未知,1:男,2:女）',
                            `campus` varchar(50) DEFAULT NULL COMMENT '校区',
                            `dormitory` varchar(50) DEFAULT NULL COMMENT '宿舍楼',
                            `credit_score` int(11) DEFAULT '100' COMMENT '信用分（0-100）',
                            `status` tinyint(1) DEFAULT '1' COMMENT '状态（0:禁用,1:正常）',
                            `role` varchar(20) DEFAULT 'USER' COMMENT '角色（USER,ADMIN）',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            UNIQUE KEY `uk_student_id` (`student_id`),
                            KEY `idx_campus` (`campus`),
                            KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 轮播图表（banner）
CREATE TABLE `banner` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
                          `title` varchar(100) NOT NULL COMMENT '标题',
                          `image_url` varchar(255) NOT NULL COMMENT '图片URL',
                          `link_url` varchar(255) DEFAULT NULL COMMENT '跳转链接',
                          `sort_order` int(11) DEFAULT '0' COMMENT '排序值（越小越靠前）',
                          `status` tinyint(1) DEFAULT '1' COMMENT '状态（0:禁用,1:启用）',
                          `start_time` datetime DEFAULT NULL COMMENT '开始展示时间',
                          `end_time` datetime DEFAULT NULL COMMENT '结束展示时间',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`),
                          KEY `idx_status_sort` (`status`, `sort_order`),
                          KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 3. 商品分类表（category）
CREATE TABLE `category` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                            `name` varchar(50) NOT NULL COMMENT '分类名称',
                            `parent_id` bigint(20) DEFAULT '0' COMMENT '父分类ID（0表示一级分类）',
                            `sort_order` int(11) DEFAULT '0' COMMENT '排序值',
                            `status` tinyint(1) DEFAULT '1' COMMENT '状态（0:禁用,1:启用）',
                            `icon` varchar(100) DEFAULT NULL COMMENT '分类图标',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_parent_status` (`parent_id`, `status`),
                            KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 4. 商品表（product）【核心修正：status默认值改为1（上架中），彻底解决一上架就售出问题】
CREATE TABLE `product` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                           `user_id` bigint(20) NOT NULL COMMENT '发布用户ID',
                           `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
                           `title` varchar(100) NOT NULL COMMENT '商品标题',
                           `description` text COMMENT '商品描述',
                           `price` decimal(10,2) NOT NULL COMMENT '价格',
                           `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
                           `cover_image` varchar(255) NOT NULL COMMENT '封面图URL',
                           `images` json DEFAULT NULL COMMENT '商品图片列表（JSON数组）',
                           `status` tinyint(1) DEFAULT '1' COMMENT '状态（0:下架,1:上架中,2:已售出,3:已删除）【默认1=上架中，新建商品自动上架】',
                           `condition` tinyint(1) DEFAULT '0' COMMENT '新旧程度（0:全新,1:几乎全新,2:轻微使用,3:明显使用痕迹）',
                           `view_count` int(11) DEFAULT '0' COMMENT '浏览数',
                           `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
                           `location` varchar(100) DEFAULT NULL COMMENT '交易地点',
                           `is_negotiable` tinyint(1) DEFAULT '0' COMMENT '是否可议价（0:否,1:是）',
                           `is_delivery` tinyint(1) DEFAULT '0' COMMENT '是否支持配送（0:否,1:是）',
                           `campus` varchar(50) DEFAULT NULL COMMENT '校区',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_user_status` (`user_id`, `status`),
                           KEY `idx_category` (`category_id`),
                           KEY `idx_price` (`price`),
                           KEY `idx_status_time` (`status`, `create_time`),
                           KEY `idx_campus` (`campus`),
                           KEY `idx_title` (`title`),
                           KEY `idx_title_category_price` (`title`, `category_id`, `price`),
                           CONSTRAINT `fk_product_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
                           CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 5. 收藏表（favorite）
CREATE TABLE `favorite` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
                            `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                            `product_id` bigint(20) NOT NULL COMMENT '商品ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
                            KEY `idx_user` (`user_id`),
                            KEY `idx_product` (`product_id`),
                            CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
                            CONSTRAINT `fk_favorite_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 6. 订单表（order）
CREATE TABLE `order` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                         `order_no` varchar(32) NOT NULL COMMENT '订单编号',
                         `buyer_id` bigint(20) NOT NULL COMMENT '买家ID',
                         `seller_id` bigint(20) NOT NULL COMMENT '卖家ID',
                         `product_id` bigint(20) NOT NULL COMMENT '商品ID',
                         `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                         `status` tinyint(1) DEFAULT '0' COMMENT '状态（0:待付款,1:待发货,2:待收货,3:已完成,4:已取消,5:退款中）',
                         `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式（ALIPAY,WECHAT,CASH）',
                         `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
                         `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
                         `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
                         `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
                         `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
                         `buyer_remark` varchar(200) DEFAULT NULL COMMENT '买家备注',
                         `seller_remark` varchar(200) DEFAULT NULL COMMENT '卖家备注',
                         `delivery_address` varchar(200) DEFAULT NULL COMMENT '配送地址',
                         `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_order_no` (`order_no`),
                         KEY `idx_buyer` (`buyer_id`),
                         KEY `idx_seller` (`seller_id`),
                         KEY `idx_product` (`product_id`),
                         KEY `idx_status` (`status`),
                         KEY `idx_create_time` (`create_time`),
                         CONSTRAINT `fk_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `sys_user` (`id`),
                         CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `sys_user` (`id`),
                         CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 7. 评价表（review）
CREATE TABLE `review` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
                          `order_id` bigint(20) NOT NULL COMMENT '订单ID',
                          `reviewer_id` bigint(20) NOT NULL COMMENT '评价者ID',
                          `target_id` bigint(20) NOT NULL COMMENT '被评价者ID',
                          `type` tinyint(1) DEFAULT '0' COMMENT '类型（0:买家评价卖家,1:卖家评价买家）',
                          `rating` tinyint(1) NOT NULL COMMENT '评分（1-5星）',
                          `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
                          `tags` varchar(200) DEFAULT NULL COMMENT '评价标签（逗号分隔）',
                          `is_anonymous` tinyint(1) DEFAULT '0' COMMENT '是否匿名（0:否,1:是）',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_order_reviewer` (`order_id`, `reviewer_id`),
                          KEY `idx_target` (`target_id`),
                          KEY `idx_reviewer` (`reviewer_id`),
                          KEY `idx_order` (`order_id`),
                          CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
                          CONSTRAINT `fk_review_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`),
                          CONSTRAINT `fk_review_target` FOREIGN KEY (`target_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';