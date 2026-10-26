package com.example.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.example.common.constants.RedisConstants.AI_CHAT_HISTORY_EXPIRE;
import static com.example.common.constants.RedisConstants.AI_CHAT_HISTORY_PREFIX;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Redis 聊天历史操作工具类
 * 提供纯 Redis 操作，不被 Spring 管理
 */
@RequiredArgsConstructor
public class RedisChatHistoryUtils {

    private final StringRedisTemplate redisTemplate;


    /**
     * 保存会话到 Redis
     */
    public void save(String type, String sessionId, Long userId) {
        if (userId == null) {
            return;
        }

        // 按用户维度存储，使用有序集合，分数为当前时间戳
        String key = AI_CHAT_HISTORY_PREFIX + type + ":" + userId;
        redisTemplate.opsForZSet().add(key, sessionId, System.currentTimeMillis());

        // 设置过期时间
        redisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 从 Redis 获取会话 ID 列表
     */
    public List<String> getChatIds(String type, Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        String key = AI_CHAT_HISTORY_PREFIX + type + ":" + userId;
        // 按分数倒序获取（最新的会话在前）
        Set<String> chatIds = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        if (chatIds == null || chatIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chatIds.stream().toList();
    }

    /**
     * 从 Redis 删除会话
     */
    public void delete(String type, String sessionId, Long userId) {
        if (userId == null) {
            return;
        }

        String key = AI_CHAT_HISTORY_PREFIX + type + ":" + userId;
        redisTemplate.opsForZSet().remove(key, sessionId);
    }
}