package com.example.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.AI_CHAT_HISTORY_EXPIRE;
import static com.example.common.constants.RedisConstants.AI_CHAT_HISTORY_PREFIX;

/**
 * 基于 Redis 的 LangChain4j ChatMemoryStore 实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = AI_CHAT_HISTORY_PREFIX + memoryId;
        try {
            List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
            
            if (jsonList == null || jsonList.isEmpty()) {
                return List.of();
            }

            List<ChatMessage> messages = jsonList.stream()
                    .map(ChatMessageDeserializer::messageFromJson)
                    .toList();

            // 刷新 TTL
            redisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, TimeUnit.MINUTES);
            
            log.debug("从 Redis 获取消息，memoryId: {}, 数量: {}", memoryId, messages.size());
            return messages;
        } catch (Exception e) {
            log.error("从 Redis 获取消息失败，memoryId: {}", memoryId, e);
            return List.of();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = AI_CHAT_HISTORY_PREFIX + memoryId;
        try {
            // 序列化消息
            List<String> jsonList = messages.stream()
                    .map(ChatMessageSerializer::messageToJson)
                    .toList();

            // 先删除旧数据，再写入新数据（保证一致性）
            redisTemplate.delete(key);
            
            if (!jsonList.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, jsonList);
            }

            // 设置过期时间
            redisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, TimeUnit.MINUTES);
            
            log.debug("更新消息到 Redis，memoryId: {}, 数量: {}", memoryId, messages.size());
        } catch (Exception e) {
            log.error("更新消息到 Redis 失败，memoryId: {}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = AI_CHAT_HISTORY_PREFIX + memoryId;
        try {
            redisTemplate.delete(key);
            log.debug("删除消息，memoryId: {}", memoryId);
        } catch (Exception e) {
            log.error("删除消息失败，memoryId: {}", memoryId, e);
        }
    }
}
