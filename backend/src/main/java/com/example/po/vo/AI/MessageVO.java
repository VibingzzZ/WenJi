package com.example.po.vo.ai;

import com.example.po.pojo.AIChatMessage;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageVO {
    private String role;
    private String content;

    /**
     * 从数据库实体 AIChatMessage 转换
     */
    public MessageVO(AIChatMessage aiChatMessage) {
        this.role = aiChatMessage.getRole().toLowerCase();
        this.content = aiChatMessage.getContent();
    }
}