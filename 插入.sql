USE `campus_secondhand`;

-- ======================================
-- 第一步：插入用户数据（密码统一为123456，BCrypt加密后真实可用）
-- ======================================
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `student_id`, `phone`, `email`, `avatar_url`, `gender`, `campus`, `dormitory`, `credit_score`) VALUES
                                                                                                                                                               ('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '张三', '20210001', '13800138001', 'zhangsan@stu.edu.cn', 'https://example.com/avatars/1.jpg', 1, '东校区', '3号楼101', 95),
                                                                                                                                                               ('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '李四', '20210002', '13800138002', 'lisi@stu.edu.cn', 'https://example.com/avatars/2.jpg', 1, '西校区', '5号楼203', 88),
                                                                                                                                                               ('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '王五', '20210003', '13800138003', 'wangwu@stu.edu.cn', 'https://example.com/avatars/3.jpg', 1, '东校区', '2号楼305', 92),
                                                                                                                                                               ('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '赵六', '20210004', '13800138004', 'zhaoliu@stu.edu.cn', 'https://example.com/avatars/4.jpg', 1, '南校区', '7号楼402', 85),
                                                                                                                                                               ('xiaohong', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '小红', '20210005', '13800138005', 'xiaohong@stu.edu.cn', 'https://example.com/avatars/5.jpg', 2, '东校区', '1号楼506', 96),
                                                                                                                                                               ('xiaoming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '小明', '20210006', '13800138006', 'xiaoming@stu.edu.cn', 'https://example.com/avatars/6.jpg', 1, '西校区', '4号楼108', 90),
                                                                                                                                                               ('meimei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '美美', '20210007', '13800138007', 'meimei@stu.edu.cn', 'https://example.com/avatars/7.jpg', 2, '南校区', '8号楼207', 93),
                                                                                                                                                               ('dahua', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '大华', '20210008', '13800138008', 'dahua@stu.edu.cn', 'https://example.com/avatars/8.jpg', 1, '东校区', '3号楼309', 87),
                                                                                                                                                               ('lanlan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '兰兰', '20210009', '13800138009', 'lanlan@stu.edu.cn', 'https://example.com/avatars/9.jpg', 2, '西校区', '6号楼104', 94),
                                                                                                                                                               ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iK6YcFvjjCw6GRAuTh3SD9vO2p4m', '系统管理员', 'admin001', '13800138100', 'admin@campus.edu.cn', 'https://example.com/avatars/admin.jpg', 0, '东校区', '行政楼', 100);

-- 设置管理员角色
UPDATE `sys_user` SET `role` = 'ADMIN' WHERE `username` = 'admin';

-- ======================================
-- 第二步：插入商品分类（一级+二级）
-- ======================================
-- 插入一级分类
INSERT INTO `category` (`name`, `parent_id`, `sort_order`, `icon`) VALUES
                                                                       ('电子产品', 0, 1, 'laptop'),
                                                                       ('图书资料', 0, 2, 'book'),
                                                                       ('生活用品', 0, 3, 'home'),
                                                                       ('服装鞋帽', 0, 4, 'tshirt'),
                                                                       ('运动器材', 0, 5, 'dumbbell'),
                                                                       ('其他物品', 0, 6, 'more');

-- 获取一级分类的ID
SET @electronics_id = (SELECT `id` FROM `category` WHERE `name` = '电子产品');
SET @books_id = (SELECT `id` FROM `category` WHERE `name` = '图书资料');
SET @daily_id = (SELECT `id` FROM `category` WHERE `name` = '生活用品');
SET @clothing_id = (SELECT `id` FROM `category` WHERE `name` = '服装鞋帽');
SET @sports_id = (SELECT `id` FROM `category` WHERE `name` = '运动器材');

-- 插入二级分类
INSERT INTO `category` (`name`, `parent_id`, `sort_order`) VALUES
-- 电子产品子分类
('手机', @electronics_id, 1),
('笔记本电脑', @electronics_id, 2),
('平板电脑', @electronics_id, 3),
('耳机/音响', @electronics_id, 4),
('充电宝/数据线', @electronics_id, 5),
-- 图书资料子分类
('教材/课本', @books_id, 1),
('考研资料', @books_id, 2),
('文学小说', @books_id, 3),
('外语学习', @books_id, 4),
-- 生活用品子分类
('台灯/风扇', @daily_id, 1),
('收纳用品', @daily_id, 2),
('洗漱用品', @daily_id, 3),
('床上用品', @daily_id, 4),
-- 服装鞋帽子分类
('上衣', @clothing_id, 1),
('裤子', @clothing_id, 2),
('鞋子', @clothing_id, 3),
('包包', @clothing_id, 4),
-- 运动器材子分类
('球类', @sports_id, 1),
('健身器材', @sports_id, 2),
('户外用品', @sports_id, 3);

-- ======================================
-- 第三步：插入轮播图
-- ======================================
INSERT INTO `banner` (`title`, `image_url`, `link_url`, `sort_order`, `status`) VALUES
                                                                                    ('毕业季闲置特卖', 'https://example.com/banners/graduation.jpg', '/product/list?tag=graduation', 1, 1),
                                                                                    ('开学季必备好物', 'https://example.com/banners/back-to-school.jpg', '/product/list?tag=back-to-school', 2, 1),
                                                                                    ('电子产品专场', 'https://example.com/banners/electronics.jpg', '/product/list?category=1', 3, 1),
                                                                                    ('二手教材大回收', 'https://example.com/banners/textbooks.jpg', '/product/list?category=2', 4, 1),
                                                                                    ('运动器材交易区', 'https://example.com/banners/sports.jpg', '/product/list?category=5', 5, 1);

-- ======================================
-- 第四步：获取用户/分类ID，用于后续插入
-- ======================================
-- 获取用户ID
SET @zhangsan_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'zhangsan');
SET @lisi_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'lisi');
SET @wangwu_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'wangwu');
SET @zhaoliu_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'zhaoliu');
SET @xiaohong_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'xiaohong');
SET @xiaoming_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'xiaoming');
SET @meimei_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'meimei');
SET @dahua_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'dahua');
SET @lanlan_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'lanlan');

-- 获取分类ID
SET @phone_cat_id = (SELECT `id` FROM `category` WHERE `name` = '手机');
SET @laptop_cat_id = (SELECT `id` FROM `category` WHERE `name` = '笔记本电脑');
SET @tablet_cat_id = (SELECT `id` FROM `category` WHERE `name` = '平板电脑');
SET @earphone_cat_id = (SELECT `id` FROM `category` WHERE `name` = '耳机/音响');
SET @textbook_cat_id = (SELECT `id` FROM `category` WHERE `name` = '教材/课本');
SET @exam_cat_id = (SELECT `id` FROM `category` WHERE `name` = '考研资料');
SET @lamp_cat_id = (SELECT `id` FROM `category` WHERE `name` = '台灯/风扇');
SET @clothing_cat_id = (SELECT `id` FROM `category` WHERE `name` = '上衣');
SET @shoes_cat_id = (SELECT `id` FROM `category` WHERE `name` = '鞋子');
SET @ball_cat_id = (SELECT `id` FROM `category` WHERE `name` = '球类');

-- ======================================
-- 第五步：插入商品数据（严格遵循status逻辑：1=上架中，2=已售出，0=下架）
-- ======================================
INSERT INTO `product` (`user_id`, `category_id`, `title`, `description`, `price`, `original_price`, `cover_image`, `images`, `status`, `condition`, `view_count`, `like_count`, `location`, `is_negotiable`, `is_delivery`, `campus`) VALUES
-- 电子产品类（8个）
(@zhangsan_id, @laptop_cat_id, '联想小新Pro14笔记本电脑', '2022年款，i5-1135G7处理器，16GB内存，512GB固态硬盘，14英寸2.8K屏幕，使用一年半，外观完好，运行流畅，适合学习和办公使用。', 3499.00, 5999.00, 'https://example.com/products/laptop1.jpg', '["https://example.com/products/laptop1.jpg", "https://example.com/products/laptop2.jpg"]', 1, 2, 156, 23, '东校区3号楼101', 1, 1, '东校区'),
(@lisi_id, @phone_cat_id, 'iPhone 12 128GB 黑色', '2021年购买，国行版，电池健康度85%，外观有轻微使用痕迹，屏幕无划痕，功能一切正常，无拆无修，配件齐全。', 2899.00, 6299.00, 'https://example.com/products/iphone12.jpg', '["https://example.com/products/iphone12.jpg", "https://example.com/products/iphone12_2.jpg"]', 1, 2, 234, 45, '西校区5号楼203', 1, 0, '西校区'),
(@wangwu_id, @tablet_cat_id, '华为MatePad 11平板电脑', '2022年购买，128GB存储，2K全面屏，支持手写笔，适合记笔记和看视频，保护得很好，几乎没有划痕。', 1899.00, 2799.00, 'https://example.com/products/matepad.jpg', '["https://example.com/products/matepad.jpg"]', 1, 1, 189, 32, '东校区2号楼305', 0, 1, '东校区'),
(@xiaohong_id, @earphone_cat_id, 'AirPods Pro 二代无线耳机', '2023年初购买，国行正品，降噪功能强大，使用不到一年，外观如新，配件齐全，包装盒都在。', 1199.00, 1999.00, 'https://example.com/products/airpods.jpg', '["https://example.com/products/airpods.jpg"]', 1, 1, 123, 18, '东校区1号楼506', 0, 1, '东校区'),
(@xiaoming_id, @phone_cat_id, '小米11 Ultra 12+256GB', '2021年旗舰机，陶瓷后盖，三星GN2大底主摄，功能完好，电池健康度82%，屏幕有轻微划痕但不影响使用。', 1999.00, 5999.00, 'https://example.com/products/mi11.jpg', '["https://example.com/products/mi11.jpg"]', 1, 2, 167, 27, '西校区4号楼108', 1, 0, '西校区'),
(@meimei_id, @laptop_cat_id, 'MacBook Air M1 8+256GB', '2021年款，M1芯片，续航超强，性能出色，轻度使用一年，外观几乎全新，原装充电器，适合编程和设计。', 5499.00, 7999.00, 'https://example.com/products/macbook.jpg', '["https://example.com/products/macbook.jpg"]', 2, 1, 312, 56, '南校区8号楼207', 0, 1, '南校区'), -- 已售出(status=2)
(@dahua_id, @earphone_cat_id, '索尼WH-1000XM4降噪耳机', '经典降噪耳机，音质出色，配件齐全，使用一年半，耳罩有正常使用痕迹，功能完好。', 1299.00, 2499.00, 'https://example.com/products/sony.jpg', '["https://example.com/products/sony.jpg"]', 1, 2, 98, 15, '东校区3号楼309', 1, 1, '东校区'),
(@lanlan_id, @laptop_cat_id, '戴尔游匣G15游戏本', 'RTX3060显卡，i7-11800H处理器，16GB内存，512GB SSD，144Hz屏幕，玩大型游戏无压力，散热良好。', 4999.00, 8999.00, 'https://example.com/products/dell.jpg', '["https://example.com/products/dell.jpg"]', 0, 2, 145, 21, '西校区6号楼104', 1, 0, '西校区'), -- 已下架(status=0)

-- 图书资料类（6个）
(@zhangsan_id, @textbook_cat_id, '高等数学（同济第七版）上下册', '大一使用的高数教材，有少量笔记和勾画，但不影响阅读，适合新生预习或复习使用。', 25.00, 68.00, 'https://example.com/products/math.jpg', '["https://example.com/products/math.jpg"]', 1, 2, 76, 8, '东校区3号楼101', 0, 1, '东校区'),
(@lisi_id, @exam_cat_id, '考研英语一全套资料', '包含近10年真题及解析、词汇书、作文模板等，内容完整，有少量笔记，适合考研同学使用。', 80.00, 200.00, 'https://example.com/products/english.jpg', '["https://example.com/products/english.jpg"]', 1, 2, 89, 12, '西校区5号楼203', 1, 1, '西校区'),
(@wangwu_id, @textbook_cat_id, '数据结构与算法（C语言版）', '计算机专业经典教材，有详细笔记和习题解答，书页干净，无缺页破损。', 35.00, 59.00, 'https://example.com/products/datastruct.jpg', '["https://example.com/products/datastruct.jpg"]', 1, 1, 67, 7, '东校区2号楼305', 0, 1, '东校区'),
(@xiaohong_id, @textbook_cat_id, '大学物理（第五版）全套', '大二物理课程教材，上下册齐全，有课堂笔记和重点标记，学习参考价值高。', 40.00, 98.00, 'https://example.com/products/physics.jpg', '["https://example.com/products/physics.jpg"]', 1, 2, 54, 6, '东校区1号楼506', 0, 1, '东校区'),
(@xiaoming_id, @exam_cat_id, '2024年公务员考试全套资料', '包含行测、申论教材、历年真题、模拟题等，资料齐全，几乎全新，因个人原因转让。', 120.00, 300.00, 'https://example.com/products/gwy.jpg', '["https://example.com/products/gwy.jpg"]', 2, 1, 112, 19, '西校区4号楼108', 1, 1, '西校区'), -- 已售出(status=2)
(@meimei_id, @textbook_cat_id, '微观经济学（曼昆）', '经济学专业经典教材，英文原版，有少量中文注释，书况良好，适合经济学爱好者。', 45.00, 128.00, 'https://example.com/products/economics.jpg', '["https://example.com/products/economics.jpg"]', 1, 2, 43, 5, '南校区8号楼207', 0, 1, '南校区'),

-- 生活用品类（5个）
(@dahua_id, @lamp_cat_id, '小米智能台灯', '可调色温和亮度，支持手机控制，使用一年，功能完好，外观有轻微使用痕迹。', 65.00, 149.00, 'https://example.com/products/lamp.jpg', '["https://example.com/products/lamp.jpg"]', 1, 2, 78, 9, '东校区3号楼309', 0, 1, '东校区'),
(@lanlan_id, @lamp_cat_id, '无叶风扇', '夏季必备，静音设计，遥控操作，使用一个夏天，清洁干净，运行良好。', 120.00, 299.00, 'https://example.com/products/fan.jpg', '["https://example.com/products/fan.jpg"]', 1, 2, 56, 7, '西校区6号楼104', 1, 0, '西校区'),
(@zhangsan_id, @lamp_cat_id, '宿舍用折叠桌', '床上学习桌，可调节高度，木质材料，结实耐用，使用半年，几乎全新。', 35.00, 79.00, 'https://example.com/products/table.jpg', '["https://example.com/products/table.jpg"]', 1, 1, 45, 6, '东校区3号楼101', 0, 0, '东校区'),
(@lisi_id, @lamp_cat_id, 'USB充电小夜灯', '宿舍床头灯，三档调光，触摸开关，使用方便，适合熬夜学习不打扰室友。', 15.00, 29.00, 'https://example.com/products/nightlight.jpg', '["https://example.com/products/nightlight.jpg"]', 1, 1, 32, 4, '西校区5号楼203', 0, 1, '西校区'),
(@wangwu_id, @lamp_cat_id, '挂墙式收纳架', '宿舍空间有限，这个收纳架可以充分利用墙面空间，金属材质，承重好。', 25.00, 49.00, 'https://example.com/products/shelf.jpg', '["https://example.com/products/shelf.jpg"]', 0, 1, 29, 3, '东校区2号楼305', 0, 0, '东校区'), -- 已下架(status=0)

-- 服装鞋帽类（5个）
(@xiaohong_id, @clothing_cat_id, '优衣库羊毛衫 L码', '冬季保暖羊毛衫，深灰色，L码适合175-180cm男生，仅穿洗过两次，几乎全新。', 80.00, 299.00, 'https://example.com/products/sweater.jpg', '["https://example.com/products/sweater.jpg"]', 1, 1, 67, 8, '东校区1号楼506', 1, 1, '东校区'),
(@xiaoming_id, @shoes_cat_id, '耐克Air Force 1 白色 42码', '经典款白色空军一号，42码，穿了一个学期，鞋底有正常磨损，已清洗干净。', 299.00, 799.00, 'https://example.com/products/nike.jpg', '["https://example.com/products/nike.jpg"]', 1, 2, 134, 22, '西校区4号楼108', 0, 1, '西校区'),
(@meimei_id, @clothing_cat_id, 'ZARA春季连衣裙 M码', '浅蓝色碎花连衣裙，M码适合160-165cm女生，仅试穿过一次，吊牌还在，全新转让。', 120.00, 399.00, 'https://example.com/products/dress.jpg', '["https://example.com/products/dress.jpg"]', 1, 0, 98, 15, '南校区8号楼207', 0, 1, '南校区'),
(@dahua_id, @shoes_cat_id, '匡威经典款帆布鞋 41码', '黑色经典款，41码，穿了一个学期，鞋面有正常褶皱，已清洗，鞋底磨损不严重。', 150.00, 439.00, 'https://example.com/products/converse.jpg', '["https://example.com/products/converse.jpg"]', 2, 2, 87, 11, '东校区3号楼309', 1, 1, '东校区'), -- 已售出(status=2)
(@lanlan_id, @clothing_cat_id, '李宁运动外套 XL码', '黑色运动外套，XL码适合180-185cm男生，防水面料，仅穿过几次，清洗干净。', 90.00, 269.00, 'https://example.com/products/jacket.jpg', '["https://example.com/products/jacket.jpg"]', 1, 1, 54, 7, '西校区6号楼104', 1, 1, '西校区'),

-- 运动器材类（4个）
(@zhangsan_id, @ball_cat_id, '斯伯丁篮球 7号标准球', '室内外通用篮球，使用一学期，有正常使用痕迹，气嘴完好，打气筒赠送。', 60.00, 199.00, 'https://example.com/products/basketball.jpg', '["https://example.com/products/basketball.jpg"]', 1, 2, 76, 10, '东校区3号楼101', 0, 0, '东校区'),
(@lisi_id, @ball_cat_id, '威尔胜羽毛球拍一对', '入门级羽毛球拍，包含拍套和两个羽毛球，适合初学者，使用痕迹轻微。', 80.00, 249.00, 'https://example.com/products/badminton.jpg', '["https://example.com/products/badminton.jpg"]', 1, 2, 45, 6, '西校区5号楼203', 1, 0, '西校区'),
(@wangwu_id, @ball_cat_id, '迪卡侬瑜伽垫', '加厚防滑瑜伽垫，紫色，使用半年，保持清洁，无破损，适合宿舍健身。', 40.00, 99.00, 'https://example.com/products/yoga.jpg', '["https://example.com/products/yoga.jpg"]', 1, 2, 38, 5, '东校区2号楼305', 0, 1, '东校区'),
(@xiaohong_id, @ball_cat_id, '小米体脂秤', '智能体脂秤，可连接手机APP，测量多项身体数据，使用一年，功能完好。', 50.00, 199.00, 'https://example.com/products/scale.jpg', '["https://example.com/products/scale.jpg"]', 0, 2, 42, 4, '东校区1号楼506', 0, 1, '东校区'); -- 已下架(status=0)

-- ======================================
-- 第六步：插入收藏记录
-- ======================================
-- 获取商品ID
SET @product1_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%联想小新%');
SET @product2_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%iPhone 12%');
SET @product3_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%华为MatePad%');
SET @product4_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%AirPods Pro%');
SET @product5_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%小米11 Ultra%');
SET @product6_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%高等数学%');
SET @product7_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%考研英语%');
SET @product8_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%数据结构%');
SET @product9_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%小米智能台灯%');
SET @product10_id = (SELECT `id` FROM `product` WHERE `title` LIKE '%耐克Air Force%');

-- 插入收藏
INSERT INTO `favorite` (`user_id`, `product_id`) VALUES
                                                     (@zhangsan_id, @product2_id), (@zhangsan_id, @product4_id), (@zhangsan_id, @product10_id),
                                                     (@lisi_id, @product1_id), (@lisi_id, @product3_id), (@lisi_id, @product8_id),
                                                     (@wangwu_id, @product2_id), (@wangwu_id, @product5_id), (@wangwu_id, @product7_id),
                                                     (@xiaohong_id, @product1_id), (@xiaohong_id, @product6_id), (@xiaohong_id, @product9_id),
                                                     (@xiaoming_id, @product3_id), (@xiaoming_id, @product4_id),
                                                     (@meimei_id, @product1_id), (@meimei_id, @product10_id),
                                                     (@dahua_id, @product5_id), (@dahua_id, @product7_id),
                                                     (@lanlan_id, @product2_id), (@lanlan_id, @product6_id);

-- ======================================
-- 第七步：插入订单数据
-- ======================================
INSERT INTO `order` (`order_no`, `buyer_id`, `seller_id`, `product_id`, `total_amount`, `status`, `payment_method`, `payment_time`, `delivery_time`, `finish_time`, `buyer_remark`, `contact_phone`, `delivery_address`) VALUES
-- 待付款订单（2个）
('202403200001', @xiaohong_id, @zhangsan_id, @product1_id, 3499.00, 0, NULL, NULL, NULL, NULL, '麻烦留一下，我今晚付款', '13800138005', '东校区1号楼506'),
('202403200002', @xiaoming_id, @lisi_id, @product2_id, 2899.00, 0, NULL, NULL, NULL, NULL, '可以小刀吗？', '13800138006', '西校区4号楼108'),

-- 待发货订单（2个）
('202403200003', @meimei_id, @xiaohong_id, @product4_id, 1199.00, 1, 'ALIPAY', '2024-03-20 10:30:00', NULL, NULL, '请包装好一点，谢谢', '13800138007', '南校区8号楼207'),
('202403200004', @dahua_id, @wangwu_id, @product3_id, 1899.00, 1, 'WECHAT', '2024-03-20 11:15:00', NULL, NULL, '什么时候可以发货？', '13800138008', '东校区3号楼309'),

-- 待收货订单（3个）
('202403200005', @lanlan_id, @xiaoming_id, @product5_id, 1999.00, 2, 'ALIPAY', '2024-03-19 14:20:00', '2024-03-20 09:30:00', NULL, '发货后麻烦给个单号', '13800138009', '西校区6号楼104'),
('202403200006', @zhangsan_id, @meimei_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%MacBook Air%'), 5499.00, 2, 'WECHAT', '2024-03-18 16:45:00', '2024-03-19 10:15:00', NULL, '希望尽快收到', '13800138001', '东校区3号楼101'),
('202403200007', @lisi_id, @dahua_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%匡威经典款%'), 150.00, 2, 'CASH', '2024-03-19 13:30:00', '2024-03-20 08:45:00', NULL, '面交，已付款', '13800138002', '西校区5号楼203'),

-- 已完成订单（5个）
('202403200008', @wangwu_id, @xiaohong_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%公务员考试%'), 120.00, 3, 'ALIPAY', '2024-03-15 09:20:00', '2024-03-15 14:30:00', '2024-03-17 16:00:00', '资料很全，谢谢', '13800138003', '东校区2号楼305'),
('202403200009', @xiaohong_id, @meimei_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%微观经济学%'), 45.00, 3, 'WECHAT', '2024-03-16 11:10:00', '2024-03-16 15:45:00', '2024-03-18 10:30:00', '书况很好', '13800138005', '东校区1号楼506'),
('202403200010', @xiaoming_id, @dahua_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%索尼WH-1000XM4%'), 1299.00, 3, 'ALIPAY', '2024-03-14 13:25:00', '2024-03-14 16:50:00', '2024-03-16 14:20:00', '降噪效果不错', '13800138006', '西校区4号楼108'),
('202403200011', @meimei_id, @lanlan_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%李宁运动外套%'), 90.00, 3, 'WECHAT', '2024-03-17 15:40:00', '2024-03-17 19:10:00', '2024-03-19 11:45:00', '尺码合适', '13800138007', '南校区8号楼207'),
('202403200012', @dahua_id, @zhangsan_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%斯伯丁篮球%'), 60.00, 3, 'CASH', '2024-03-18 10:15:00', '2024-03-18 14:30:00', '2024-03-19 16:20:00', '球的质量很好', '13800138008', '东校区3号楼309'),

-- 已取消订单（3个）
('202403200013', @lanlan_id, @lisi_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%威尔胜羽毛球拍%'), 80.00, 4, NULL, NULL, NULL, '2024-03-19 17:30:00', '找到更合适的了', '13800138009', '西校区6号楼104'),
('202403200014', @zhangsan_id, @wangwu_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%迪卡侬瑜伽垫%'), 40.00, 4, NULL, NULL, NULL, '2024-03-20 09:45:00', '计划有变，暂时不需要了', '13800138001', '东校区3号楼101'),
('202403200015', @lisi_id, @xiaohong_id, (SELECT `id` FROM `product` WHERE `title` LIKE '%优衣库羊毛衫%'), 80.00, 4, NULL, NULL, NULL, '2024-03-20 12:20:00', '尺寸不合适', '13800138002', '西校区5号楼203');

-- ======================================
-- 第八步：插入评价数据
-- ======================================
-- 获取已完成订单的ID
SET @order8_id = (SELECT `id` FROM `order` WHERE `order_no` = '202403200008');
SET @order9_id = (SELECT `id` FROM `order` WHERE `order_no` = '202403200009');
SET @order10_id = (SELECT `id` FROM `order` WHERE `order_no` = '202403200010');
SET @order11_id = (SELECT `id` FROM `order` WHERE `order_no` = '202403200011');
SET @order12_id = (SELECT `id` FROM `order` WHERE `order_no` = '202403200012');

-- 插入评价
INSERT INTO `review` (`order_id`, `reviewer_id`, `target_id`, `type`, `rating`, `content`, `tags`, `is_anonymous`) VALUES
-- 买家评价卖家
(@order8_id, @wangwu_id, @xiaohong_id, 0, 5, '资料非常齐全，卖家很细心，包装得很好，还送了一些额外的学习资料，非常感谢！', '资料齐全,包装细心,诚信卖家', 0),
(@order9_id, @xiaohong_id, @meimei_id, 0, 4, '书保存得很好，笔记清晰，对我学习很有帮助，价格也很实惠。', '书况良好,价格实惠,有帮助', 0),
(@order10_id, @xiaoming_id, @dahua_id, 0, 5, '耳机音质很棒，降噪效果很好，卖家描述很准确，交易过程很愉快。', '音质好,描述准确,交易愉快', 0),
(@order11_id, @meimei_id, @lanlan_id, 0, 4, '外套质量不错，尺码合适，清洗得很干净，穿着很舒服。', '质量好,尺码合适,干净整洁', 0),
(@order12_id, @dahua_id, @zhangsan_id, 0, 5, '篮球弹性很好，气嘴完好，卖家还送了打气筒，非常周到。', '弹性好,配件齐全,服务周到', 0),

-- 卖家评价买家
(@order8_id, @xiaohong_id, @wangwu_id, 1, 5, '买家很爽快，付款及时，沟通顺畅，非常好的交易体验。', '付款及时,沟通顺畅,好评买家', 0),
(@order9_id, @meimei_id, @xiaohong_id, 1, 5, '买家很有礼貌，确认收货很快，希望以后还有机会交易。', '有礼貌,确认快,好评买家', 0),
(@order10_id, @dahua_id, @xiaoming_id, 1, 4, '交易过程顺利，买家对产品了解，沟通效率高。', '交易顺利,沟通高效', 0),
(@order11_id, @lanlan_id, @meimei_id, 1, 5, '买家很好说话，没有过多纠结，交易愉快。', '好说话,交易愉快', 0),
(@order12_id, @zhangsan_id, @dahua_id, 1, 5, '面交很顺利，买家守时，希望篮球能帮到他锻炼身体。', '守时,面交顺利', 0);

