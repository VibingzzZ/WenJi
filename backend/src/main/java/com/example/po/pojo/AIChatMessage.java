package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 聊天消息实体
 * 用于持久化存储用户的对话内容，支持后续的：
 * 1. 向量嵌入（Embedding）- 用于语义搜索和 RAG
 * 2. 用户画像分析 - 用于个性化推荐
 * 3. 智能体训练 - 用于 fine-tuning
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("ai_chat_message")
public class AIChatMessage {
    @TableId(type = IdType.AUTO)
    private Long messageId;        // 主键 ID

    private String sessionId;        // 会话 ID

    private String role;           // 角色：user/assistant/system

    private String content;        // 消息内容（重要：用于向量化）

    private String messageType;    // 消息类型：text/image/multi-modal

    private String toolCalls;      // 工具调用记录（JSON 格式）

    private String metadata;       // 元数据（图片 URL、坐标、情感分析结果等 JSON）

    private LocalDateTime createTime; // 创建时间

    private String contentHash;    // 内容指纹（sessionId + role + 正文 的 MD5），归档幂等去重用

    /**
     * 以下字段用于 AI 分析和推荐
     */
    private String embedding;      // 向量嵌入（JSON 数组，如 [0.1, 0.2, ...]）

    private String keywords;       // 提取的关键词（JSON 数组，用于主题分析）

    private String sentiment;      // 情感分析：positive/negative/neutral

    private Integer topicCategory; // 主题分类：1-历史文化 2-非遗技艺 3-旅游攻略 4-其他
}