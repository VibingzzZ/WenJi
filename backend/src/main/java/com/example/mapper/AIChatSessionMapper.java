package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.pojo.AIChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 聊天会话 Mapper 接口
 */
@Mapper
public interface AIChatSessionMapper extends BaseMapper<AIChatSession> {
    // 可以使用 MyBatis-Plus 提供的基础方法
    // 如需自定义 SQL，可在 XML 中实现
}
