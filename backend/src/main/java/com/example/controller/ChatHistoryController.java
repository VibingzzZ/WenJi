package com.example.controller;


import com.example.common.Result;
import com.example.po.pojo.AIChatMessage;
import com.example.repository.impl.DatabaseChatHistoryRepository;
import com.example.po.vo.ai.MessageVO;
import com.example.po.vo.ai.ChatSessionVO;

import com.example.mapper.AIChatSessionMapper;
import com.example.po.pojo.AIChatSession;
import com.example.service.AIChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.common.utils.UersUtils.getCurrentUserId;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
@Tag(name = "AI聊天历史管理", description = "AI聊天历史记录相关接口")
public class ChatHistoryController {

    private final AIChatMessageService aiChatMessageService;
    private final AIChatSessionMapper chatSessionMapper;


    @Autowired
    private DatabaseChatHistoryRepository databaseChatHistoryRepository;
    /**
     * 获取聊天 ID 列表，根据类型获取所有聊天会话 ID
     * @param type 聊天类型，例如 "text" 或 "image"
     * @return 包含所有聊天会话 ID 的列表
     */
    @Operation(summary = "获取聊天 ID 列表", description = "根据类型获取所有聊天会话 ID")
    @GetMapping("/{type}/list")
    public Result<List<String>> getChatIds(@Parameter(description = "聊天类型") @PathVariable("type") String type){
        log.debug("收到获取聊天历史列表请求，type: {}", type);
        List<String> chatIds = databaseChatHistoryRepository.getChatIds(type);
        log.debug("获取到 {} 个会话 ID", chatIds.size());
        return Result.success(chatIds);
    }

    /**
     * 获取会话详情
     * @param type 会话类型
     * @param sessionId 会话 ID
     * @return 会话详情
     */
    @Operation(summary = "获取聊天历史详情", description = "根据类型和会话 ID 获取完整的聊天历史记录")
    @GetMapping("/{type}/{sessionId}")
    public Result<List<ChatSessionVO>> getChatHistory(
            @Parameter(description = "聊天类型") @PathVariable("type") String type,
            @Parameter(description = "会话 ID") @PathVariable("sessionId") String sessionId){
        log.debug("收到获取聊天历史详情请求，type: {}, sessionId: {}", type, sessionId);

        // 1. 获取当前登录用户 ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录");
            return Result.error("请先登录后查看历史记录", null);
        }

        // 2. 验证 sessionId 是否属于当前用户
        AIChatSession session = chatSessionMapper.selectById(sessionId);
        // 2.1 检查会话是否存在
        if (session == null) {
            log.warn("会话不存在，sessionId: {}", sessionId);
            return Result.error("会话不存在，请检查会话ID", null);
        }
        // 2.2 检查会话是否属于当前用户
        if (!session.getUserId().equals(userId)) {
            log.warn("无权访问该会话，sessionId: {}, 当前用户：{}, 会话所属用户：{}", sessionId, userId, session.getUserId());
            return Result.error("无权访问该会话", null);
        }

        // 3. 从 MySQL 中获取消息
        List<AIChatMessage> messages = aiChatMessageService.getMessagesBySessionId(sessionId, userId);
        if(messages==null){
            messages = List.of();
        }
        log.debug("从 MySQL 获取到 {} 条消息", messages.size());
        // 4. 转换为 MessageVO 列表
        List<MessageVO> messageVOs = messages.stream().map(MessageVO::new).toList();
        // 5. 转换为 ChatSessionVO 列表
        ChatSessionVO sessionVO = new ChatSessionVO(sessionId, LocalDateTime.now(), messageVOs);
        List<ChatSessionVO> sessionList = List.of(sessionVO);
        log.debug("返回会话详情，sessionId: {}, 消息数量: {}", sessionId, messageVOs.size());
        return Result.success(sessionList);
    }


    /**
     * 删除会话
     * @param sessionId 会话 ID
     */
    @Operation(summary = "删除对话", description = "删除指定的对话会话")
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(
            @Parameter(description = "会话 ID") @PathVariable("sessionId") String sessionId) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录", null);
            }

            databaseChatHistoryRepository.delete("chat", sessionId, userId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除会话失败，sessionId: {}", sessionId, e);
            return Result.error(e.getMessage(), null);
        }
    }
}