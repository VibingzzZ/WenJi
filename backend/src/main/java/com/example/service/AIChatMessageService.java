package com.example.service;

import com.example.po.pojo.AIChatMessage;

import java.util.List;

/**
 * AI 聊天消息服务接口
 * 负责对话内容的持久化存储，支持向量化处理和个性化推荐
 */
public interface AIChatMessageService {

    /**
     * 保存单条消息到数据库
     * @param message 消息对象
     * @return 是否成功
     */
    boolean saveMessage(AIChatMessage message);

    /**
     * 批量保存消息（用于异步归档）
     * @param messages 消息列表
     * @return 影响行数
     */
    int batchSaveMessages(List<AIChatMessage> messages);

    /**
     * 根据会话 ID 查询消息列表
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @return 消息列表
     */
    List<AIChatMessage> getMessagesBySessionId(String sessionId, Long userId);

    /**
     * 查询用户的历史消息（用于向量分析和推荐）
     * @param userId 用户 ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<AIChatMessage> getUserHistoryMessages(Long userId, Integer limit);

    /**
     * 更新消息的向量嵌入（用于语义搜索）
     * @param messageId 消息 ID
     * @param embedding 向量数据（JSON 数组字符串）
     * @return 是否成功
     */
    boolean updateEmbedding(Long messageId, String embedding);

    /**
     * 更新消息的关键词和主题（用于用户画像分析）
     * @param messageId 消息 ID
     * @param keywords 关键词（JSON 数组）
     * @param topicCategory 主题分类
     * @return 是否成功
     */
    boolean updateKeywordsAndTopic(Long messageId, String keywords, Integer topicCategory);

    /**
     * 更新消息的情感分析结果
     * @param messageId 消息 ID
     * @param sentiment 情感：positive/negative/neutral
     * @return 是否成功
     */
    boolean updateSentiment(Long messageId, String sentiment);
}