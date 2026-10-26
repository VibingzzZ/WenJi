package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ai_chat_session")
public class AIChatSession {
    @TableId(type = IdType.INPUT)
    private String sessionId;    // 会话 ID (VARCHAR类型,与数据库表结构匹配)

    private Long userId;       // 关联 User.userId (INT类型,与数据库表结构匹配)
    private String currentLocation;  // 用户当前位置(经纬度)
    private String sessionContext;   // 会话上下文(JSON格式)
    private Integer messageCount;    // 消息数量
    private LocalDateTime startTime; // 会话开始时间
    private LocalDateTime lastActiveTime; // 最后活跃时间
    private String status;           // 状态: active/ended
}