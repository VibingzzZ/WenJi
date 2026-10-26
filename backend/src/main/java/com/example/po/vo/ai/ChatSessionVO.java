package com.example.po.vo.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatSessionVO {
    private String sessionId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    private List<MessageVO> messages;

    public ChatSessionVO(String sessionId, LocalDateTime startTime, List<MessageVO> messages) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.messages = messages;
    }
}