package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mapper.AIChatMessageMapper;
import com.example.mapper.AIChatSessionMapper;
import com.example.po.pojo.AIChatMessage;
import com.example.po.pojo.AIChatSession;
import com.example.repository.RedisChatMemory;
import com.example.service.AIChatMessageService;
import dev.langchain4j.data.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 聊天消息服务实现类
 * 核心功能：
 * 1. 实时保存会话元数据到 ai_chat_session 表
 * 2. 异步归档对话内容到 ai_chat_message 表（避免影响实时性能）
 * 3. 支持向量化处理和个性化推荐的数据准备
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatMessageServiceImpl implements AIChatMessageService {

    private final AIChatMessageMapper chatMessageMapper;

    private final AIChatSessionMapper chatSessionMapper;
    @Autowired
    private RedisChatMemory redisChatMemory;


    /**
     * 保存消息
     * @param message 消息对象
     * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMessage(AIChatMessage message) {
        try {
            log.info("开始保存消息，sessionId: {}, role: {}, content: {}",
                message.getSessionId(), message.getRole(), message.getContent());

            int result = chatMessageMapper.insert(message);
            log.info("保存消息成功，影响行数: {}, messageId: {}, sessionId: {}",
                result, message.getMessageId(), message.getSessionId());

            return result > 0;
        } catch (Exception e) {
            log.error("保存消息失败，sessionId: {}, role: {}",
                message.getSessionId(), message.getRole(), e);
            return false;
        }
    }

    /**
     * 批量保存消息
     * @param messages 消息列表
     * @return 保存成功的消息数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveMessages(List<AIChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        try {
            int totalInserted = chatMessageMapper.batchInsert(messages);
            log.info("批量保存消息成功，总数：{}", totalInserted);
            return totalInserted;
        } catch (Exception e) {
            log.error("批量保存消息失败", e);
            throw new RuntimeException("批量保存失败", e);
        }
    }

    /**
     * 根据会话 ID 查询消息列表
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @return 消息列表
     */
    @Override
    public List<AIChatMessage> getMessagesBySessionId(String sessionId, Long userId) {
        log.info("查询消息，sessionId: {}, userId: {}", sessionId, userId);

        try {
            List<AIChatMessage> result = chatMessageMapper.selectBySessionId(sessionId);
            log.info("查询结果：{} 条消息", result != null ? result.size() : 0);
            if (result != null && !result.isEmpty()) {
                log.info("第一条消息 ID: {}, role: {}", result.get(0).getMessageId(), result.get(0).getRole());
            }
            return result != null ? result : List.of();
        } catch (Exception e) {
            log.error("查询消息失败，sessionId: {}", sessionId, e);
            return List.of();
        }
    }

    /**
     * 根据用户 ID 查询用户历史消息
     * @param userId 用户 ID
     * @param limit 查询数量限制
     * @return 历史消息列表
     **/
    @Override
    public List<AIChatMessage> getUserHistoryMessages(Long userId, Integer limit) {
        try {
            return chatMessageMapper.selectUserHistoryMessages(userId, limit);
        } catch (Exception e) {
            log.error("查询用户历史消息失败", e);
            return List.of();
        }
    }

    /**
     * 更新消息的向量嵌入
     * @param messageId 消息 ID
     * @param embedding 向量嵌入
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmbedding(Long messageId, String embedding) {
        try {
            AIChatMessage message = chatMessageMapper.selectById(messageId);
            if (message == null) {
                log.warn("消息不存在，messageId: {}", messageId);
                return false;
            }

            message.setEmbedding(embedding);
            int result = chatMessageMapper.updateById(message);

            log.info("更新向量嵌入成功，messageId: {}", messageId);
            return result > 0;
        } catch (Exception e) {
            log.error("更新向量嵌入失败", e);
            return false;
        }
    }

    /**
     * 批量更新消息的关键词和主题
     * @param messageId 消息 ID
     * @param keywords 关键词
     * @param topicCategory 主题分类
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateKeywordsAndTopic(Long messageId, String keywords, Integer topicCategory) {
        try {
            //查询消息是否存在
            AIChatMessage message = chatMessageMapper.selectById(messageId);
            if (message == null) {
                log.warn("消息不存在，messageId: {}", messageId);
                return false;
            }
            //更新关键词和主题
            message.setKeywords(keywords);
            message.setTopicCategory(topicCategory);
            int result = chatMessageMapper.updateById(message);

            log.info("更新关键词和主题成功，messageId: {}", messageId);
            return result > 0;
        } catch (Exception e) {
            log.error("更新关键词和主题失败", e);
            return false;
        }
    }

    /**
     * 批量更新消息的的情感分析结果
     * @param messageId 消息 ID
     * @param sentiment 情感分析结果
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSentiment(Long messageId, String sentiment) {
        try {
            AIChatMessage message = chatMessageMapper.selectById(messageId);
            if (message == null) {
                log.warn("消息不存在，messageId: {}", messageId);
                return false;
            }
            //更新情感分析结果
            message.setSentiment(sentiment);
            int result = chatMessageMapper.updateById(message);

            log.info("更新情感分析成功，messageId: {}", messageId);
            return result > 0;
        } catch (Exception e) {
            log.error("更新情感分析失败", e);
            return false;
        }
    }

    /**
     * 定时任务：每 5 分钟归档一次活跃会话的消息
     * 从 Redis 的 ChatMemory 中读取消息，批量保存到数据库
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledArchiveMessages() {
        log.info("开始执行定时任务：归档聊天消息");

        try {
            // 1. 获取所有活跃的会话（从 ai_chat_session 表查询最近 5 分钟活跃的）
            List<String> activeSessionIds = getActiveSessionIds();
            log.info("发现 {} 个活跃会话", activeSessionIds.size());

            if (activeSessionIds.isEmpty()) {
                log.info("没有活跃会话，跳过本次归档");
                return;
            }

            // 2. 遍历每个会话，从 Redis 读取消息并归档
            int totalArchived = 0;
            for (String sessionId : activeSessionIds) {
                int archived = archiveSessionMessages(sessionId);
                totalArchived += archived;
            }

            log.info("定时归档任务执行完成，共归档 {} 条消息", totalArchived);
        } catch (Exception e) {
            log.error("定时归档任务执行失败", e);
        }
    }

    /**
     * 归档单个会话的消息
     *
     * @param sessionId 会话 ID
     * @return 批量保存的条数
     */
    private int archiveSessionMessages(String sessionId) {
        try {
            // 从 Redis 读取该会话的所有消息
            List<ChatMessage> messages = redisChatMemory.getMessages(sessionId);

            if (messages.isEmpty()) {
                log.debug("会话没有新消息，sessionId: {}", sessionId);
                return 0;
            }

            // 转换为 AIChatMessage 实体
            List<AIChatMessage> chatMessages = messages.stream()
                    .map(msg -> convertToAIChatMessage(msg, sessionId))
                    .toList();

            // 批量保存到数据库
            if (!chatMessages.isEmpty()) {
                int saved = batchSaveMessages(chatMessages);
                log.info("归档会话消息成功，sessionId: {}, 保存 {} 条", sessionId, saved);
                //归档成功后，可以直接从 Redis 中清理旧数据
                redisChatMemory.deleteMessages(sessionId);
                return saved;
            }
        } catch (Exception e) {
            log.error("归档会话消息失败，sessionId: {}", sessionId, e);
        }
        return 0;
    }

    /**
     * 获取活跃的会话 ID 列表
     */
    private List<String> getActiveSessionIds() {
        // 查询最近 5 分钟内活跃的会话
        try {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

            QueryWrapper<AIChatSession> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "active")
                    .ge("last_active_time", fiveMinutesAgo)
                    .orderByDesc("last_active_time");

            List<AIChatSession> sessions = chatSessionMapper.selectList(queryWrapper);

            //返回 sessionId 列表
            return sessions.stream()
                    .map(AIChatSession::getSessionId)
                    .toList();
        } catch (Exception e) {
            log.error("查询活跃会话失败",e);
            return List.of();
        }
    }

    /**
     * 转换 Spring AI Message 到 AIChatMessage（对象转化）
     * @param message Spring AI Message
     * @param sessionId 会话 ID
     * @return AIChatMessage
     */
    private AIChatMessage convertToAIChatMessage(ChatMessage message, String sessionId) {
        return AIChatMessage.builder()
                .sessionId(sessionId)
                .role(message.type().name().toLowerCase())
                .content(message.toString())
                .messageType("text")
                .createTime(LocalDateTime.now())
                .build();
    }
}