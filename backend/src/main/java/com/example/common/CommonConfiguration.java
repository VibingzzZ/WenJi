package com.example.common;

import com.example.repository.RedisChatMemoryStore;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 聊天记忆提供者
     * 用于 LangChain4j @AiService 自动管理对话历史
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
    }

    /**
     * RAG 内容检索器
     * 用于从向量数据库中检索相关知识
     * 
     * 配置说明：
     * - maxResults: 每次检索召回的最相关知识数量（默认 3 条）
     * - minScore: 最低相似度阈值（0.6），低于此值的知识不会被召回
     */
    @Bean
    public ContentRetriever contentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(redisEmbeddingStore)
                .maxResults(3)
                .minScore(0.6)
                .build();
    }
}
