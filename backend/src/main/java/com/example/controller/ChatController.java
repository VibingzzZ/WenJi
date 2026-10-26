package com.example.controller;


import com.example.repository.impl.DatabaseChatHistoryRepository;
import com.example.service.AIChatMessageService;
import com.example.po.pojo.AIChatMessage;
import com.example.service.aiservice.ConsultantService;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.example.common.utils.UersUtils.getCurrentUserId;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/user/ai")
@Tag(name = "AI 聊天管理", description = "AI 聊天相关接口")
public class ChatController {

    private final ConsultantService consultantService;
    private final DatabaseChatHistoryRepository databaseChatHistoryRepository;
    private final AIChatMessageService chatMessageService;
    private final com.example.service.aiservice.RagEnhancedChatService ragChatService;

    /**
     * AI 聊天接口（支持文本和多模态，流式响应）
     */
    @Operation(summary = "AI 聊天", description = "支持文本和图片，返回流式响应")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @Parameter(description = "用户问题") @RequestParam("prompt") String prompt,
            @Parameter(description = "会话 ID") @RequestParam(value = "chatId", required = false) String chatId,
            @Parameter(description = "图片文件列表") @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        log.info("收到聊天请求，prompt: {}, chatId: {}, images: {}", prompt, chatId, images != null ? images.size() : 0);

        if (chatId == null || chatId.isEmpty()) {
            chatId = "chat_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
            log.info("生成新会话 ID: {}", chatId);
        }

        // 保存会话记录
        try {
            Long userId = getCurrentUserId();
            if (userId != null) {
                databaseChatHistoryRepository.save("chat", chatId, userId);
                AIChatMessage userMessage = AIChatMessage.builder()
                        .sessionId(chatId)
                        .role("user")
                        .content(prompt)
                        .messageType(images != null && !images.isEmpty() ? "multi-modal" : "text")
                        .createTime(LocalDateTime.now())
                        .build();
                chatMessageService.saveMessage(userMessage);
            }
        } catch (Exception e) {
            log.error("保存会话记录失败: {}", e.getMessage());
        }

        // 根据是否有图片选择调用方法
        if (images != null && !images.isEmpty()) {
            return chatWithImages(chatId, prompt, images);
        } else {
            String finalChatId = chatId;
            // 使用 RAG 增强聊天（优先查 Redis，未命中降级为普通 LLM）
            return ragChatService.chatWithRAG(chatId, prompt)
                    .doOnNext(chunk -> log.debug("收到 chunk: {}", chunk))
                    .doOnComplete(() -> log.info("AI 回复完成，sessionId: {}", finalChatId));
        }
    }

    /**
     * 多模态聊天处理
     */
    private Flux<String> chatWithImages(String chatId, String prompt, List<MultipartFile> images) {
        try {
            // 构建多模态消息内容列表
            List<Content> contents = new ArrayList<>();
            
            // 添加文本内容
            contents.add(TextContent.from(prompt));
            
            // 添加图片内容
            for (MultipartFile file : images) {
                String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                String mimeType = file.getContentType();
                contents.add(ImageContent.from(base64Image, mimeType));
                log.debug("添加图片，mimeType: {}, size: {}", mimeType, file.getSize());
            }

            return consultantService.chatWithImage(chatId, contents)
                    .doOnNext(chunk -> log.debug("收到 chunk: {}", chunk))
                    .doOnComplete(() -> log.info("多模态 AI 回复完成，sessionId: {}", chatId));
        } catch (Exception e) {
            log.error("处理图片失败", e);
            return Flux.error(e);
        }
    }
}
