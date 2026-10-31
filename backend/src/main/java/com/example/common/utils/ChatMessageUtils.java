package com.example.common.utils;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LangChain4j 消息与持久化文本之间的编解码工具。
 *
 * <p>两个方向：
 * <ul>
 *   <li>入库方向：{@link #plainText(ChatMessage)} 取出可读正文，避免把 {@code UserMessage { contents = ... }}
 *       这种 toString 结果写进数据库。</li>
 *   <li>回填方向：{@link #toChatMessage(String, String)} 按 role 还原成 LangChain4j 的消息对象。</li>
 * </ul>
 *
 * <p>另外提供内容指纹 {@link #contentHash}，供归档幂等去重使用。
 */
public final class ChatMessageUtils {

    /** 历史数据里可能存在 toString 形式的正文，形如：TextContent { text = "..." } */
    private static final Pattern SERIALIZED_TEXT =
            Pattern.compile("text = \"((?:[^\"\\\\]|\\\\.)*)\"");

    private ChatMessageUtils() {
    }

    /**
     * 取出消息的可读正文。多模态消息里的图片用占位符表示。
     */
    public static String plainText(ChatMessage message) {
        if (message == null) {
            return "";
        }
        if (message instanceof UserMessage userMessage) {
            StringBuilder sb = new StringBuilder();
            for (Content content : userMessage.contents()) {
                if (content instanceof TextContent textContent) {
                    sb.append(textContent.text());
                } else if (content instanceof ImageContent) {
                    sb.append("[图片]");
                }
            }
            return sb.toString();
        }
        if (message instanceof AiMessage aiMessage) {
            return aiMessage.text() == null ? "" : aiMessage.text();
        }
        if (message instanceof SystemMessage systemMessage) {
            return systemMessage.text();
        }
        if (message instanceof ToolExecutionResultMessage toolMessage) {
            return toolMessage.text();
        }
        return message.toString();
    }

    /**
     * 按 role 还原成 LangChain4j 消息对象。
     * 库里存的 role 有三种：user / ai / system。
     */
    public static ChatMessage toChatMessage(String role, String content) {
        String text = stripSerialized(content);
        String normalized = role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "user" -> UserMessage.from(text);
            case "system" -> SystemMessage.from(text);
            // ai / assistant 统一按 AI 回复处理
            default -> AiMessage.from(text);
        };
    }

    /**
     * 兼容历史数据：如果正文是 toString 形式，抽出其中的纯文本，否则原样返回。
     */
    public static String stripSerialized(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        if (!content.startsWith("UserMessage") && !content.startsWith("AiMessage")
                && !content.startsWith("SystemMessage")) {
            return content;
        }
        Matcher matcher = SERIALIZED_TEXT.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group(1).replace("\\\"", "\"").replace("\\n", "\n"));
        }
        return sb.length() == 0 ? content : sb.toString();
    }

    /**
     * 消息内容指纹：会话 + 角色 + 正文 三者决定唯一性。
     * 用于归档去重，避免「归档成功但清 Redis 失败」后整批重复入库。
     */
    public static String contentHash(String sessionId, String role, String plainText) {
        String raw = (sessionId == null ? "" : sessionId)
                + "|" + (role == null ? "" : role)
                + "|" + (plainText == null ? "" : plainText);
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
