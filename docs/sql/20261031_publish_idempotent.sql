-- ============================================================
-- 发布游记幂等改造 DDL
-- 目标库：wenji
-- 执行方式：mysql -uroot -p wenji < docs/sql/20261031_publish_idempotent.sql
-- 注意：MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报 duplicate 错误，
--       属于预期，跳过对应的那条即可。
-- ============================================================

-- ---------- 0. 前置检查：确认没有挡路的重复数据 ----------
-- 三条都应返回空结果，若有输出，先清理再建唯一索引。

-- 0.1 同一用户重复的徽章（会导致 uniq_user_badge 建不上）
SELECT user_id, badge_id, COUNT(*) AS cnt
FROM user_badge
GROUP BY user_id, badge_id
HAVING cnt > 1;

-- 0.2 同一用户同一来源的重复流水（会导致 uniq_user_source_biz 建不上）
-- biz_id 尚未创建，这里只检查 source + description 组合
SELECT user_id, source, description, COUNT(*) AS cnt
FROM point_record
GROUP BY user_id, source, description
HAVING cnt > 1;


-- ---------- 1. travel_blog：正文指纹 + 唯一索引 ----------
ALTER TABLE travel_blog
    ADD COLUMN content_hash CHAR(32) NULL COMMENT '正文MD5，与 user_id 组成唯一键做发布幂等';

ALTER TABLE travel_blog
    ADD UNIQUE KEY uniq_user_content (user_id, content_hash);

-- 历史数据 content_hash 为 NULL，MySQL 唯一索引允许多个 NULL，不会冲突。


-- ---------- 2. point_record：业务幂等键 + 唯一索引 ----------
ALTER TABLE point_record
    ADD COLUMN biz_id VARCHAR(64) NULL COMMENT '业务幂等键，形如 blog:123 / badge:5';

ALTER TABLE point_record
    ADD UNIQUE KEY uniq_user_source_biz (user_id, source, biz_id);


-- ---------- 3. user_badge：并发兜底 ----------
-- 徽章逻辑本身已有 earnedBadgeIds 判断，这条索引只防并发双插。
ALTER TABLE user_badge
    ADD UNIQUE KEY uniq_user_badge (user_id, badge_id);


-- ---------- 4. 确认 record_id 是自增主键 ----------
-- 实体里 record_id 标了 IdType.AUTO，若表上没设自增，insert 会失败。
SHOW CREATE TABLE point_record;


-- ============================================================
-- 验收 SQL（改造后跑三条业务场景，用这几句核对）
-- ============================================================
-- 场景一：同一内容连发两次 → 应只有 1 条博客、1 条流水、experience 只加 10
-- SELECT COUNT(*) FROM travel_blog WHERE user_id = ? AND content_hash = ?;
-- SELECT COUNT(*) FROM point_record WHERE user_id = ? AND source = 'blog';
-- SELECT experience FROM user WHERE user_id = ?;
--
-- 场景二：不同内容连发两次 → 应有 2 条博客、2 条流水、experience 加 20
--
-- 场景三：并发打 10 次同一请求 → 库里仍只有 1 条，且无 500
