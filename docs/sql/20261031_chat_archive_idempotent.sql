-- ============================================================
--  对话消息归档幂等改造
--  表：ai_chat_message
--  目的：定时归档任务重复执行时，不产生重复消息记录
--  执行：mysql -uroot -p wenji < docs/sql/20261031_chat_archive_idempotent.sql
--  建议：先单独跑「第 0 步」看清现状，再整体执行
-- ============================================================

-- ------------------------------------------------------------
-- 第 0 步：前置检查（只读，不改数据）
-- ------------------------------------------------------------

-- 0.1 总条数
SELECT COUNT(*) AS total_messages FROM ai_chat_message;

-- 0.2 是否已经有重复消息（同会话 + 同角色 + 同正文）
--     有输出 = 需要执行第 2 步清理，否则唯一索引会建不上
SELECT MD5(CONCAT(session_id, '|', role, '|', content)) AS content_hash,
       COUNT(*)                                          AS dup_count
FROM ai_chat_message
GROUP BY content_hash
HAVING COUNT(*) > 1;

-- 0.3 历史数据里是否存在 toString 形式的脏正文
--     （用户历史记录页会显示成 UserMessage { contents = ... }）
SELECT COUNT(*) AS serialized_rows
FROM ai_chat_message
WHERE content LIKE 'UserMessage {%' OR content LIKE 'AiMessage {%' OR content LIKE 'SystemMessage {%';


-- ------------------------------------------------------------
-- 第 1 步：加内容指纹列
-- ------------------------------------------------------------
ALTER TABLE ai_chat_message
    ADD COLUMN content_hash CHAR(32) NULL COMMENT '内容指纹：MD5(sessionId|role|content)，归档幂等去重';

-- ------------------------------------------------------------
-- 第 2 步：清理重复消息（保留最早的一条）
--          MySQL 不允许在 DELETE 的子查询里直接引用同一张表，
--          所以用 JOIN 派生表的写法
-- ------------------------------------------------------------
DELETE t
FROM ai_chat_message t
         JOIN (SELECT MIN(message_id)                              AS keep_id,
                      MD5(CONCAT(session_id, '|', role, '|', content)) AS h
               FROM ai_chat_message
               GROUP BY h
               HAVING COUNT(*) > 1) d
              ON MD5(CONCAT(t.session_id, '|', t.role, '|', t.content)) = d.h
WHERE t.message_id <> d.keep_id;

-- ------------------------------------------------------------
-- 第 3 步：回填历史行的指纹
--          Java 侧用 Spring DigestUtils.md5Hex（UTF-8），与 MySQL MD5() 结果一致
-- ------------------------------------------------------------
UPDATE ai_chat_message
SET content_hash = MD5(CONCAT(session_id, '|', role, '|', content))
WHERE content_hash IS NULL;

-- ------------------------------------------------------------
-- 第 4 步：建唯一索引
-- ------------------------------------------------------------
ALTER TABLE ai_chat_message
    ADD UNIQUE KEY uniq_content_hash (content_hash);


-- ------------------------------------------------------------
-- 第 5 步：验证
-- ------------------------------------------------------------

-- 5.1 索引是否生效
SHOW INDEX FROM ai_chat_message WHERE Key_name = 'uniq_content_hash';

-- 5.2 是否还有未回填的行（应为 0）
SELECT COUNT(*) AS null_hash_rows FROM ai_chat_message WHERE content_hash IS NULL;

-- 5.3 手工验证幂等：重复插入同一行应被静默跳过，不报错
--     把 <sessionId> 换成真实会话 ID 后连跑两次，第二次受影响行为 0
-- INSERT INTO ai_chat_message (session_id, role, content, content_hash, message_type, create_time)
-- VALUES ('<sessionId>', 'user', '幂等测试', MD5(CONCAT('<sessionId>', '|', 'user', '|', '幂等测试')), 'text', NOW())
-- ON DUPLICATE KEY UPDATE message_id = message_id;
