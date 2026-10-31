package com.example.repository;

import com.example.common.utils.ChatMessageUtils;
import com.example.mapper.AIChatMessageMapper;
import com.example.po.pojo.AIChatMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.AI_CHAT_HISTORY_EXPIRE;
import static com.example.common.constants.RedisConstants.AI_CHAT_HISTORY_PREFIX;

/**
 * 基于 Redis 的 LangChain4j ChatMemoryStore 实现。
 *
 * <p>数据分层：
 * <ul>
 *   <li>热数据：Redis List，一条消息一个元素，承载当前会话的上下文窗口</li>
 *   <li>冷数据：MySQL {@code ai_chat_message}，由定时归档任务写入</li>
 * </ul>
 *
 * <p>热数据未命中（归档已清理 / 服务重启）时，从冷存储按会话取最近的消息回填 Redis，
 * 保证多轮上下文不会因为一次归档或一次重启就丢失。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    /** 回填条数，与 MessageWindowChatMemory 的 maxMessages 保持一致 */
    private static final int RESTORE_LIMIT = 20;

    private final StringRedisTemplate redisTemplate;

    private final AIChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = AI_CHAT_HISTORY_PREFIX + memoryId;
        try {
            List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);

            if (jsonList != null && !jsonList.isEmpty()) {
                List<ChatMessage> messages = jsonList.stream()
                        .map(ChatMessageDeserializer::messageFromJson)
                        .toList();

                // 刷新 TTL：常量本身是秒，必须用 SECONDS
                redisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, TimeUnit.SECONDS);

                log.debug("从 Redis 获取消息，memoryId: {}, 数量: {}", memoryId, messages.size());
                return messages;
            }

            // 热数据未命中 → 从 MySQL 回填，并把结果写回 Redis
            return restoreFromDatabase(memoryId, true);
        } catch (Exception e) {
            // Redis 不可用时仍然尝试直读 MySQL，保证对话上下文不中断
            log.error("从 Redis 获取消息失败，降级为直读 MySQL，memoryId: {}", memoryId, e);
            try {
                return restoreFromDatabase(memoryId, false);
            } catch (Exception dbException) {
                log.error("从 MySQL 回填对话历史同样失败，memoryId: {}", memoryId, dbException);
                return List.of();
            }
        }
    }

    /**
     * 从 MySQL 读取某会话的最近消息。
     *
     * @param memoryId 会话 ID
     * @param writeBack 是否把结果回写 Redis
     */
    private List<ChatMessage> restoreFromDatabase(Object memoryId, boolean writeBack) {
        String sessionId = String.valueOf(memoryId);
        List<AIChatMessage> rows = chatMessageMapper.selectRecentBySessionId(sessionId, RESTORE_LIMIT);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        // 查询结果是按时间倒序，回填需要正序
        List<ChatMessage> messages = new ArrayList<>(rows.size());
        for (int i = rows.size() - 1; i >= 0; i--) {
            AIChatMessage row = rows.get(i);
            messages.add(ChatMessageUtils.toChatMessage(row.getRole(), row.getContent()));
        }

        // 末尾若是没有 AI 回复的用户消息，说明正是当前这一问，去掉避免重复入上下文
        while (!messages.isEmpty() && messages.get(messages.size() - 1) instanceof dev.langchain4j.data.message.UserMessage) {
            messages.remove(messages.size() - 1);
        }

        if (messages.isEmpty()) {
            return List.of();
        }

        if (writeBack) {
            updateMessages(memoryId, messages);
        }

        log.info("从 MySQL 回填对话上下文，sessionId: {}, 数量: {}, 回写 Redis: {}",
                sessionId, messages.size(), writeBack);
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = AI_CHAT_HISTORY_PREFIX + memoryId;
        try {
            List<String> jsonList = messages == null ? List.of() : messages.stream()
                    .map(ChatMessageSerializer::messageToJson)
                    .toList();

            // 先删除旧数据，再写入新数据（保证一致性）
            redisTemplate.delete(key);

            if (!jsonList.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, jsonList);
                redisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, TimeUnit.SECONDS);
            }

            log.debug("更新消息到 Redis，memoryId: {}, 数量: {}", memoryId, jsonList.size());
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
