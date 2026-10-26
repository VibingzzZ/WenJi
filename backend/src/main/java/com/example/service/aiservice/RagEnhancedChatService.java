package com.example.service.aiservice;

import com.example.po.pojo.DocumentInput;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.common.constants.RedisConstants.HIGH_SIMILARITY_THRESHOLD;
import static com.example.common.constants.RedisConstants.MIN_SIMILARITY_THRESHOLD;

/**
 * RAG 增强聊天服务
 * 
 * 核心逻辑：
 * 1. 用户提问 → 向量化
 * 2. 优先查询 Redis 向量缓存（相似度 > 0.85 直接返回）
 * 3. Redis 未命中 → 查询 EmbeddingStore
 * 4. 检索到知识 → 组装上下文 + LLM 生成答案
 * 5. 未检索到知识 → 降级为普通 LLM 回答
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagEnhancedChatService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ConsultantService consultantService;


    /**
     * RAG 增强聊天（流式输出）
     * 
     * @param chatId 会话 ID
     * @param question 用户问题
     * @return 流式响应
     */
    public Flux<String> chatWithRAG(String chatId, String question) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: 向量化用户问题
            log.debug("开始向量化问题: {}", question);
            Embedding queryEmbedding = embeddingModel.embed(question).content();
            
            // Step 2: 在 EmbeddingStore 中检索相关知识
            log.debug("开始检索相关知识...");
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5)  // 召回 Top-5
                    .minScore(MIN_SIMILARITY_THRESHOLD)
                    .build();
            
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            List<Content> contents = searchResult.matches().stream()
                    .map(match -> Content.from(match.embedded()))//转成符合Langchain4j的Content格式
                    .toList();
            
            long retrievalTime = System.currentTimeMillis() - startTime;
            log.info("检索完成，耗时: {}ms, 召回数量: {}", retrievalTime, contents.size());
            
            // Step 3: 判断是否检索到足够相关的知识
            if (contents.isEmpty()) {
                log.info("未检索到相关知识，降级为普通 LLM 回答");
                return consultantService.chatStream(chatId, question);
            }
            
            // 检查最高相似度
            double maxSimilarity = searchResult.matches().stream()
                    .mapToDouble(EmbeddingMatch::score)
                    .max()
                    .orElse(0.0);
            
            log.info("最高相似度: {}", maxSimilarity);
            
            // Step 4: 组装增强上下文
            String context = contents.stream()
                    .map(Content::textSegment)
                    .map(TextSegment::text)
                    .collect(Collectors.joining("\n---\n"));
            
            log.debug("组装的上下文长度: {}", context.length());
            
            // Step 5: 构建增强 Prompt
            String augmentedPrompt = buildAugmentedPrompt(question, context, maxSimilarity);
            
            // Step 6: 调用 LLM 生成答案（流式）
            log.info("开始调用 LLM 生成答案...");
            return consultantService.chatStream(chatId, augmentedPrompt)
                    .doOnComplete(() -> {
                        long totalTime = System.currentTimeMillis() - startTime;
                        log.info("RAG 聊天完成，总耗时: {}ms", totalTime);
                    })
                    .onErrorResume(error -> {
                        log.error("RAG 聊天失败，降级为普通聊天", error);
                        return consultantService.chatStream(chatId, question);
                    });
                    
        } catch (Exception e) {
            log.error("RAG 处理异常，降级为普通聊天", e);
            return consultantService.chatStream(chatId, question);
        }
    }

    /**
     * 构建增强的 Prompt
     * 
     * @param question 用户问题
     * @param context 检索到的知识上下文
     * @param maxSimilarity 最高相似度
     * @return 增强后的 Prompt
     */
    private String buildAugmentedPrompt(String question, String context, double maxSimilarity) {
        String sourceHint = maxSimilarity > HIGH_SIMILARITY_THRESHOLD 
                ? "以下参考资料与你的问题高度相关，请优先基于这些资料回答："
                : "以下参考资料可能与你的问题相关，请参考并结合你的知识回答：";
        
        return String.format("""
                %s
                
                【参考资料】
                %s
                
                【回答要求】
                1. 如果参考资料足以回答问题，请基于参考资料回答
                2. 如果参考资料不足，可以补充你的通用知识
                3. 回答要简洁明了，符合导游"智游"的风格
                4. 可以适当引用资料来源（如"根据相关资料..."）
                
                【用户问题】
                %s
                """, sourceHint, context, question);
    }

    /**
     * 文档入库（简单文本）
     * 
     * @param content 文档内容
     */
    public void ingestDocument(String content) {
        TextSegment segment = TextSegment.from(content);
        Embedding embedding = embeddingModel.embed(segment).content();
        String id = embeddingStore.add(embedding, segment);
        log.info("文档入库完成, id: {}, 长度: {}", id, content.length());
    }

    /**
     * 文档入库（带元数据）
     * 
     * @param content 文档内容
     * @param title 标题
     * @param source 来源
     */
    public void ingestDocument(String content, String title, String source) {
        dev.langchain4j.data.document.Metadata metadata = new dev.langchain4j.data.document.Metadata();
        metadata.put("title", title);
        metadata.put("source", source);
        
        TextSegment segment = TextSegment.from(content, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        String id = embeddingStore.add(embedding, segment);
        log.info("文档入库完成, id: {}, title: {}, 长度: {}", id, title, content.length());
    }

    /**
     * 批量文档入库
     * 
     * @param documents 文档列表（每个文档包含 content, title, source）
     */
    public void ingestDocuments(List<DocumentInput> documents) {
        log.info("开始批量入库，文档数量: {}", documents.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (DocumentInput doc : documents) {
            try {
                ingestDocument(doc.getContent(), doc.getTitle(), doc.getSource());
                successCount++;
            } catch (Exception e) {
                log.error("文档入库失败: {}", doc.getTitle(), e);
                failCount++;
            }
        }
        
        log.info("批量入库完成，成功: {}, 失败: {}", successCount, failCount);
    }


}
