package com.example.service.aiservice;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.util.List;

@AiService
public interface ConsultantService {
    /**
     * 阻塞式输出
     * @param message
     * @return
     */
    @SystemMessage(fromResource = "System.txt")
    String chat(@UserMessage String message);

    /**
     * 流式输出
     * @param message   信息
     * @return          返回
     */
    @SystemMessage(fromResource = "System.txt")
    Flux<String> chatStream(@MemoryId String chatId, @UserMessage String message);

    /**
     * 带图片的流式输出
     * @param chatId    会话id
     * @param contents  图片内容
     * @return          返回
     */
    @SystemMessage(fromResource = "System.txt")
    Flux<String> chatWithImage(@MemoryId String chatId, @UserMessage List<Content> contents);
}
