package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.pojo.AIChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 聊天消息 Mapper 接口
 * 用于持久化存储用户的对话内容，支持后续的向量化处理和个性化推荐
 */
@Mapper
public interface AIChatMessageMapper extends BaseMapper<AIChatMessage> {

    /**
     * 根据会话 ID 查询消息列表（按时间排序）
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    List<AIChatMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 批量插入消息（用于异步归档）
     * @param messages 消息列表
     * @return 影响行数
     */
    int batchInsert(@Param("messages") List<AIChatMessage> messages);

    /**
     * 查询用户的所有历史消息（用于向量嵌入和模型训练）
     * @param userId 用户 ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<AIChatMessage> selectUserHistoryMessages(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询指定时间范围内的消息（用于数据分析）
     * @param userId 用户 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<AIChatMessage> selectMessagesByTimeRange(
        @Param("userId") Long userId,
        @Param("startTime") java.time.LocalDateTime startTime,
        @Param("endTime") java.time.LocalDateTime endTime
    );
}
