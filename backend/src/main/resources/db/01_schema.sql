-- ============================================================
-- 文迹 WenJi 数据库完整初始化脚本
-- 说明：
--   1. 本脚本与 com.example.po.pojo 下的实体类字段一一对应，可重复执行
--   2. 执行顺序：01_schema.sql（建库建表） -> 02_init_data.sql（初始化数据）
--   3. 历史遗留脚本 03/04/05 的修复内容已全部合并进本脚本，无需再执行
-- ============================================================

CREATE DATABASE IF NOT EXISTS `wenji` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `wenji`;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号（登录账号）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（MD5）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '常驻之地',
    `gender` INT DEFAULT 2 COMMENT '性别 1:男 0:女 2:保密',
    `experience` INT DEFAULT 0 COMMENT '阅历值',
    `level` VARCHAR(20) DEFAULT '见习学徒' COMMENT '等级',
    `status` VARCHAR(10) DEFAULT '1' COMMENT '状态 0:禁用 1:正常',
    `is_real_name_verified` INT DEFAULT 0 COMMENT '是否实名认证 0:未认证 1:已认证',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 遗产景点表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `heritage_sites` (
    `site_id` VARCHAR(20) NOT NULL COMMENT '站点ID',
    `site_code` VARCHAR(20) DEFAULT NULL COMMENT '站点编码',
    `name` VARCHAR(100) NOT NULL COMMENT '站点名称',
    `en_name` VARCHAR(100) DEFAULT NULL COMMENT '站点英文名称',
    `type` INT DEFAULT NULL COMMENT '站点类型 1-博物馆 2-古建筑 3-自然景观 4-园林',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `level` VARCHAR(20) DEFAULT NULL COMMENT '级别 国家级/省级/市级',
    `province_code` VARCHAR(10) DEFAULT NULL COMMENT '省份编码',
    `city_code` VARCHAR(10) DEFAULT NULL COMMENT '市编码',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    `location_point` POINT NOT NULL SRID 4326 COMMENT '空间位置（POINT(经度 纬度)）',
    `geohash` VARCHAR(20) DEFAULT NULL COMMENT 'Geohash编码',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
    `description` TEXT COMMENT '描述',
    `history` TEXT COMMENT '历史',
    `techniques` TEXT COMMENT '技艺',
    `best_season` VARCHAR(50) DEFAULT NULL COMMENT '最佳季节',
    `suitable_duration` INT DEFAULT NULL COMMENT '建议游览时长(小时)',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `official_url` VARCHAR(500) DEFAULT NULL COMMENT '官方网站',
    `status` INT DEFAULT 1 COMMENT '状态 1-正常 0-关闭',
    `is_recommended` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    `popularity` DECIMAL(10, 2) DEFAULT 0 COMMENT '人气值',
    `visit_count` INT DEFAULT 0 COMMENT '访问次数',
    `rating` DECIMAL(3, 2) DEFAULT NULL COMMENT '评分',
    `capacity` INT DEFAULT NULL COMMENT '容量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`site_id`),
    SPATIAL KEY `idx_location` (`location_point`),
    KEY `idx_city_code` (`city_code`),
    KEY `idx_category` (`category`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='遗产景点表';

-- ------------------------------------------------------------
-- 3. 景点图片表
--    实体 SiteImages 的 @TableName 是 site_image，
--    而 SiteImageMapper.xml 里写的是 site_images，
--    因此建实体表 site_image，再用同名视图兼容 XML。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `site_image` (
    `image_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `site_id` VARCHAR(20) NOT NULL COMMENT '站点ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '图片标题',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '图片描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_cover` TINYINT(1) DEFAULT 0 COMMENT '是否封面',
    `image_type` INT DEFAULT 1 COMMENT '图片类型 1-照片 2-手绘 3-文物',
    `width` INT DEFAULT NULL COMMENT '宽度',
    `height` INT DEFAULT NULL COMMENT '高度',
    `size` INT DEFAULT NULL COMMENT '文件大小(KB)',
    `upload_by` VARCHAR(50) DEFAULT NULL COMMENT '上传人',
    `status` INT DEFAULT 1 COMMENT '状态 1-正常 0-隐藏',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`image_id`),
    KEY `idx_site_id` (`site_id`),
    KEY `idx_sort` (`site_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点图片表';

CREATE OR REPLACE VIEW `site_images` AS SELECT * FROM `site_image`;

-- ------------------------------------------------------------
-- 4. 景点开放时间表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `opening_hours` (
    `hour_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `site_id` VARCHAR(20) NOT NULL COMMENT '站点ID',
    `day_of_week` INT NOT NULL COMMENT '星期 1-7（周一至周日）',
    `open_time` TIME DEFAULT NULL COMMENT '开放时间',
    `close_time` TIME DEFAULT NULL COMMENT '闭馆时间',
    `is_open` TINYINT(1) DEFAULT 1 COMMENT '当天是否开放',
    `special_date` DATE DEFAULT NULL COMMENT '特殊日期',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`hour_id`),
    KEY `idx_site_id` (`site_id`),
    KEY `idx_day` (`site_id`, `day_of_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放时间表';

-- ------------------------------------------------------------
-- 5. AI 聊天会话表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `session_id` VARCHAR(50) NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '会话标题',
    `type` VARCHAR(20) DEFAULT 'chat' COMMENT '会话类型',
    `current_location` VARCHAR(100) DEFAULT NULL COMMENT '当前位置',
    `session_context` TEXT COMMENT '会话上下文',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态 active/ended',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_last_active` (`user_id`, `last_active_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

-- ------------------------------------------------------------
-- 6. AI 聊天消息表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` VARCHAR(50) NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色 user/assistant/system',
    `content` TEXT COMMENT '消息内容',
    `message_type` VARCHAR(20) DEFAULT 'text' COMMENT '消息类型 text/image/multi-modal',
    `tool_calls` TEXT COMMENT '工具调用记录(JSON)',
    `metadata` TEXT COMMENT '元数据(JSON)',
    `embedding` LONGTEXT COMMENT '向量嵌入(JSON数组)',
    `keywords` VARCHAR(500) DEFAULT NULL COMMENT '关键词(JSON数组)',
    `sentiment` VARCHAR(20) DEFAULT NULL COMMENT '情感 positive/negative/neutral',
    `topic_category` INT DEFAULT NULL COMMENT '主题分类 1-历史文化 2-非遗技艺 3-旅游攻略 4-其他',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`message_id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';

-- ------------------------------------------------------------
-- 7. 旅行笔记表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `travel_blog` (
    `blog_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `site_id` VARCHAR(50) DEFAULT NULL COMMENT '景点ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '正文',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '地点名称',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片列表(JSON字符串)',
    `experience_earned` INT DEFAULT 0 COMMENT '本次获得的阅历值',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `status` INT DEFAULT 1 COMMENT '状态 1-正常 0-隐藏',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`blog_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_site_id` (`site_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅行笔记表';

-- ------------------------------------------------------------
-- 8. 徽章定义表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `badge` (
    `badge_id` INT NOT NULL AUTO_INCREMENT COMMENT '徽章ID',
    `name` VARCHAR(100) NOT NULL COMMENT '徽章名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '徽章描述',
    `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '徽章图标URL',
    `type` VARCHAR(50) NOT NULL COMMENT '徽章类型 blog/footprint',
    `threshold` INT NOT NULL COMMENT '达成条件阈值',
    `experience_bonus` INT DEFAULT 0 COMMENT '经验奖励',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`badge_id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='徽章定义表';

-- ------------------------------------------------------------
-- 9. 用户徽章表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_badge` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `badge_id` INT NOT NULL COMMENT '徽章ID（关联 badge.badge_id）',
    `get_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户徽章表';

-- ------------------------------------------------------------
-- 10. 阅历积分流水表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `point_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `type` TINYINT NOT NULL COMMENT '类型 1-获得 2-消耗',
    `source` VARCHAR(50) NOT NULL COMMENT '来源 blog/badge/mall_exchange',
    `experience` INT NOT NULL COMMENT '变动阅历值',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- ------------------------------------------------------------
-- 11. 区域表
--     实体 Region 的 @TableName 是 region，RegionMapper.xml 用 regions，
--     同样建实体表 + 兼容视图。
--     polygon 存 WKT 文本（POINT(经度 纬度) 顺序），供 ST_Contains 使用。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `region` (
    `region_code` VARCHAR(20) NOT NULL COMMENT '区域编码',
    `parent_code` VARCHAR(20) DEFAULT NULL COMMENT '父级编码',
    `name` VARCHAR(100) NOT NULL COMMENT '区域名称',
    `level` INT NOT NULL COMMENT '层级 1-省 2-市 3-区',
    `center_lat` DECIMAL(10, 7) DEFAULT NULL COMMENT '中心纬度',
    `center_lng` DECIMAL(10, 7) DEFAULT NULL COMMENT '中心经度',
    `polygon` TEXT COMMENT '边界多边形(WKT)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`region_code`),
    KEY `idx_parent_code` (`parent_code`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';

CREATE OR REPLACE VIEW `regions` AS SELECT * FROM `region`;

-- ------------------------------------------------------------
-- 12. 图片识别日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `image_recognition_log` (
    `recognition_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '识别ID',
    `session_id` VARCHAR(50) DEFAULT NULL COMMENT '关联会话ID',
    `user_id` INT DEFAULT NULL COMMENT '用户ID',
    `image_hash` VARCHAR(64) DEFAULT NULL COMMENT '图片哈希(去重)',
    `site_id` VARCHAR(20) DEFAULT NULL COMMENT '识别出的景点ID',
    `confidence` DECIMAL(5, 4) DEFAULT NULL COMMENT '识别置信度 0-1',
    `original_image_url` VARCHAR(500) DEFAULT NULL COMMENT '原始图片路径',
    `recognition_result` TEXT COMMENT '识别结果详情(JSON)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`recognition_id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_image_hash` (`image_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片识别日志表';

-- ------------------------------------------------------------
-- 13. 工具调用日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tool_usage_log` (
    `tool_usage_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工具使用ID',
    `session_id` VARCHAR(50) DEFAULT NULL COMMENT '会话ID',
    `tool_type` VARCHAR(50) DEFAULT NULL COMMENT 'map_navigation/ar_view/weather/booking',
    `tool_parameters` TEXT COMMENT '调用参数(JSON)',
    `call_result` TEXT COMMENT '调用结果',
    `success` TINYINT(1) DEFAULT 1 COMMENT '是否成功',
    `call_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    PRIMARY KEY (`tool_usage_id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_tool_type` (`tool_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具调用日志表';

-- ------------------------------------------------------------
-- 14. 用户偏好表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_preference` (
    `preference_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `preference_type` VARCHAR(50) NOT NULL COMMENT 'interest_theme/language/pace/accessibility',
    `preference_value` TEXT COMMENT '偏好值(JSON)',
    `weight` INT DEFAULT 1 COMMENT '权重',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`preference_id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `preference_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好表';

-- ------------------------------------------------------------
-- 15. 操作日志表（AOP 切面写入，可选）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `operate_log` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `operate_emp_id` INT DEFAULT NULL COMMENT '操作员工ID',
    `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `class_name` VARCHAR(100) DEFAULT NULL COMMENT '类名',
    `method_name` VARCHAR(100) DEFAULT NULL COMMENT '方法名',
    `method_params` TEXT COMMENT '方法参数',
    `return_value` TEXT COMMENT '返回值',
    `cost_time` BIGINT DEFAULT NULL COMMENT '执行时长(ms)',
    PRIMARY KEY (`id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
