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
public class RedisChatMemory implements ChatMemoryStore {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = AI_CHAT_HISTORY_PREFIX + memoryId;
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            
            if (json == null || json.isEmpty()) {
                return List.of();
            }

            List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(json);
            
            // 刷新 TTL
            stringRedisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, TimeUnit.MINUTES);
            
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
            if (messages == null || messages.isEmpty()) {
                stringRedisTemplate.delete(key);
                return;
            }

            String json = ChatMessageSerializer.messagesToJson(messages);
            stringRedisTemplate.opsForValue().set(key, json, AI_CHAT_HISTORY_EXPIRE, TimeUnit.MINUTES);
            
            log.debug("更新消息到 Redis，memoryId: {}, 数量: {}", memoryId, messages.size());
        } catch (Exception e) {
            log.error("更新消息到 Redis 失败，memoryId: {}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = AI_CHAT_HISTORY_PREFIX + memoryId;
        try {
            stringRedisTemplate.delete(key);
            log.debug("删除消息，memoryId: {}", memoryId);
        } catch (Exception e) {
            log.error("删除消息失败，memoryId: {}", memoryId, e);
        }
    }
}