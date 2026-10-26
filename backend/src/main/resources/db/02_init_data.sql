-- ============================================================
-- 文迹 WenJi 初始化数据脚本
-- 前置：先执行 01_schema.sql
-- 特点：全部使用幂等写法，可重复执行
-- ============================================================

USE `wenji`;

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 遗产景点（10 条：北京 6 + 清远 5）
--    注意：location_point 使用 SRID 4326，MySQL 的地理坐标轴序是 POINT(纬度 经度)
-- ------------------------------------------------------------
INSERT INTO `heritage_sites`
(`site_id`, `site_code`, `name`, `type`, `category`, `level`, `province_code`, `city_code`, `address`,
 `latitude`, `longitude`, `location_point`, `cover_image`, `description`, `history`,
 `best_season`, `suitable_duration`, `is_recommended`, `popularity`, `rating`, `status`)
VALUES
('bj1', 'BJ001', '故宫博物院', 1, '博物馆', '国家级', '110000', '110100', '北京市东城区景山前街4号',
 39.9163000, 116.3972000, ST_GeomFromText('POINT(39.9163 116.3972)', 4326), '/images/gugong-taihedian.jpg',
 '中国最大的古代文化艺术博物馆，世界五大宫殿之首',
 '故宫又称紫禁城，是明清两代的皇家宫殿，是世界上现存规模最大、保存最为完整的木质结构古建筑群。',
 '春秋两季', 4, 1, 9800.00, 4.90, 1),
('bj2', 'BJ002', '中国国家博物馆', 1, '博物馆', '国家级', '110000', '110100', '北京市东城区东长安街16号',
 39.9054000, 116.3976000, ST_GeomFromText('POINT(39.9054 116.3976)', 4326), '/images/bj2中国国家博物馆.jpg',
 '世界上单体建筑面积最大的博物馆，藏品143万余件',
 '中国国家博物馆是世界上单体建筑面积最大的博物馆，馆藏文物143万余件。',
 '全年', 3, 1, 8600.00, 4.80, 1),
('bj3', 'BJ003', '天坛', 2, '古建筑', '国家级', '110000', '110100', '北京市东城区天坛内东里7号',
 39.8822000, 116.4066000, ST_GeomFromText('POINT(39.8822 116.4066)', 4326), '/images/tiantan-qiniandian.jpg',
 '明清两代皇帝祭天的场所，世界文化遗产',
 '天坛始建于明永乐年间，是明清两代皇帝祭天、祈谷的场所。',
 '春秋两季', 3, 1, 7200.00, 4.70, 1),
('bj4', 'BJ004', '颐和园', 4, '皇家园林', '国家级', '110000', '110100', '北京市海淀区新建宫门路19号',
 39.9999000, 116.2755000, ST_GeomFromText('POINT(39.9999 116.2755)', 4326), '/images/yiheyuan-changlang.jpg',
 '中国现存规模最大的皇家园林，世界文化遗产',
 '颐和园是中国现存规模最大、保存最完整的皇家园林。',
 '夏季', 4, 1, 8100.00, 4.80, 1),
('bj5', 'BJ005', '首都博物馆', 1, '博物馆', '国家级', '110000', '110100', '北京市西城区复兴门外大街16号',
 39.9073000, 116.3516000, ST_GeomFromText('POINT(39.9073 116.3516)', 4326), '/images/bj5首都博物馆.jpg',
 '展示北京历史文化的综合性博物馆',
 '首都博物馆是一座展示北京历史文化的综合性博物馆。',
 '全年', 2, 1, 4300.00, 4.60, 1),
('bj6', 'BJ006', '雍和宫', 2, '古建筑', '国家级', '110000', '110100', '北京市东城区雍和宫大街12号',
 39.9474000, 116.4175000, ST_GeomFromText('POINT(39.9474 116.4175)', 4326), '/images/bj6雍和宫.jpg',
 '北京最大的藏传佛教寺院，曾是雍正皇帝府邸',
 '雍和宫原为雍正皇帝府邸，后改为藏传佛教寺院，是北京最大的藏传佛教寺院。',
 '春秋两季', 2, 1, 5600.00, 4.60, 1),
('qy1', 'QY001', '清远市博物馆', 1, '博物馆', '市级', '440000', '441800', '清远市清城区银泉北路',
 23.7066000, 113.0541000, ST_GeomFromText('POINT(23.7066 113.0541)', 4326), '/images/qy1清远市博物馆.jpg',
 '展示清远历史文化，收藏大量珍贵文物',
 '清远市博物馆是一座综合性博物馆，收藏有大量反映清远历史文化的珍贵文物，是了解清远历史的重要窗口。',
 '全年', 2, 1, 2100.00, 4.50, 1),
('qy2', 'QY002', '飞来寺', 2, '古建筑', '省级', '440000', '441800', '清远市清城区飞来峡',
 23.6892000, 113.1234000, ST_GeomFromText('POINT(23.6892 113.1234)', 4326), '/images/qy2飞来寺.jpg',
 '始建于梁武帝时期，千年古刹，岭南名寺',
 '飞来寺始建于南朝梁武帝时期，距今已有1500多年历史，是岭南地区著名的佛教圣地。',
 '秋季', 2, 1, 1800.00, 4.40, 1),
('qy3', 'QY003', '连州地下河', 3, '自然景观', '国家级', '440000', '441800', '清远市连州市东陂镇',
 24.7806000, 112.3814000, ST_GeomFromText('POINT(24.7806 112.3814)', 4326), '/images/qy3连州地下河.jpg',
 '典型的亚热带喀斯特地貌溶洞，被誉为广东地下第一河',
 '连州地下河是一处典型的亚热带喀斯特地貌溶洞，洞内河流蜿蜒，钟乳石千姿百态。',
 '夏季', 3, 1, 3600.00, 4.70, 1),
('qy4', 'QY004', '英德宝晶宫', 3, '自然景观', '省级', '440000', '441800', '清远市英德市燕子岩',
 24.1856000, 113.4012000, ST_GeomFromText('POINT(24.1856 113.4012)', 4326), '/images/qy4英德宝晶宫.jpg',
 '集溶洞、湖泊、山林于一体的综合性景区',
 '宝晶宫是一处集溶洞、湖泊、山林于一体的综合性景区，洞内石笋、石柱、石幔千姿百态。',
 '夏季', 3, 1, 2400.00, 4.50, 1),
('qy5', 'QY005', '瑶族文化博物馆', 1, '博物馆', '省级', '440000', '441800', '清远市连南瑶族自治县',
 24.1706000, 112.0876000, ST_GeomFromText('POINT(24.1706 112.0876)', 4326), '/images/qy5瑶族文化博物馆.jpg',
 '展示瑶族历史文化，体验瑶族风情',
 '瑶族文化博物馆位于连南瑶族自治县，是全国首家瑶族专题博物馆。',
 '全年', 2, 1, 1500.00, 4.40, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `cover_image` = VALUES(`cover_image`),
  `city_code` = VALUES(`city_code`),
  `description` = VALUES(`description`),
  `history` = VALUES(`history`);

-- ------------------------------------------------------------
-- 2. 景点封面图（写入 site_image，image_type=1 表示照片）
-- ------------------------------------------------------------
INSERT INTO `site_image` (`site_id`, `image_url`, `thumbnail_url`, `title`, `sort_order`, `is_cover`, `image_type`, `status`)
SELECT s.`site_id`, s.`cover_image`, s.`cover_image`, s.`name`, 0, 1, 1, 1
FROM `heritage_sites` s
WHERE s.`cover_image` IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `site_image` i WHERE i.`site_id` = s.`site_id` AND i.`is_cover` = 1
  );

-- ------------------------------------------------------------
-- 3. 开放时间（每天 09:00-17:00；博物馆类周一闭馆）
-- ------------------------------------------------------------
INSERT INTO `opening_hours` (`site_id`, `day_of_week`, `open_time`, `close_time`, `is_open`)
SELECT s.`site_id`, d.`day_of_week`, '09:00:00', '17:00:00', 1
FROM `heritage_sites` s
JOIN (
    SELECT 1 AS `day_of_week` UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
) d
WHERE NOT EXISTS (
    SELECT 1 FROM `opening_hours` h
    WHERE h.`site_id` = s.`site_id` AND h.`day_of_week` = d.`day_of_week`
);

UPDATE `opening_hours` h
JOIN `heritage_sites` s ON s.`site_id` = h.`site_id`
SET h.`is_open` = 0, h.`remark` = '周一闭馆（法定节假日除外）'
WHERE s.`type` = 1 AND h.`day_of_week` = 1;

-- ------------------------------------------------------------
-- 4. 区域数据（供 RegionMapper.getCityByLocation 做落点判断）
--    polygon 为 WKT，轴序 POINT/POLYGON(经度 纬度)
-- ------------------------------------------------------------
INSERT INTO `region`
(`region_code`, `parent_code`, `name`, `level`, `center_lat`, `center_lng`, `polygon`, `sort_order`, `is_active`)
VALUES
('110000', NULL, '北京市', 1, 39.9042000, 116.4074000, NULL, 1, 1),
('110100', '110000', '北京市', 2, 39.9042000, 116.4074000,
 'POLYGON((115.42 39.44, 117.51 39.44, 117.51 41.06, 115.42 41.06, 115.42 39.44))', 1, 1),
('440000', NULL, '广东省', 1, 23.1319000, 113.2665000, NULL, 2, 1),
('440100', '440000', '广州市', 2, 23.1291000, 113.2644000,
 'POLYGON((113.15 22.53, 114.05 22.53, 114.05 23.93, 113.15 23.93, 113.15 22.53))', 2, 1),
('441800', '440000', '清远市', 2, 23.6817000, 113.0560000,
 'POLYGON((111.92 23.52, 113.15 23.52, 113.15 25.20, 111.92 25.20, 111.92 23.52))', 3, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `center_lat` = VALUES(`center_lat`),
  `center_lng` = VALUES(`center_lng`),
  `polygon` = VALUES(`polygon`),
  `is_active` = VALUES(`is_active`);

-- ------------------------------------------------------------
-- 5. 徽章定义
-- ------------------------------------------------------------
INSERT INTO `badge` (`name`, `description`, `icon_url`, `type`, `threshold`, `experience_bonus`)
VALUES
('初试啼声', '发布第一篇旅游心得', '/icons/badge-first-blog.png', 'blog', 1, 50),
('足迹遍布', '在 5 个不同的景点打卡', '/icons/badge-footprint-5.png', 'footprint', 5, 200),
('资深驴友', '累计发布 10 篇旅游心得', '/icons/badge-blog-10.png', 'blog', 10, 500),
('文化传承者', '在非遗类景点打卡', '/icons/badge-culture.png', 'footprint', 1, 100)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `icon_url` = VALUES(`icon_url`),
  `type` = VALUES(`type`),
  `threshold` = VALUES(`threshold`),
  `experience_bonus` = VALUES(`experience_bonus`);

-- ------------------------------------------------------------
-- 6. 演示账号：手机号 13800138000 / 密码 123456（MD5）
-- ------------------------------------------------------------
INSERT INTO `user` (`phone`, `username`, `password`, `real_name`, `location`, `gender`, `experience`, `level`, `status`)
VALUES ('13800138000', '文迹行者', 'e10adc3949ba59abbe56e057f20f883e', '廖展滨', '广东广州', 1, 60, '见习学徒', '1')
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`), `password` = VALUES(`password`);
