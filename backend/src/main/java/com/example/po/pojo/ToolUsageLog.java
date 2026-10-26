package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tool_usage_log")
public class ToolUsageLog {
    private Long toolUsageId;        // 工具使用ID
    private String sessionId;        // 会话ID
    private String toolType;         // map_navigation/ar_view/weather/booking
    private String toolParameters;   // 调用参数(JSON)
    private String callResult;       // 调用结果
    private Boolean success;         // 是否成功
    private LocalDateTime callTime;  // 调用时间
}