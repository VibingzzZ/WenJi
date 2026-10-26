package com.example.service.aiservice;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final ContentRetriever contentRetriever;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ConsultantService consultantService;

    /**
     * 带知识库检索的对话（流式输出）
     * 先检索相关文档 → 拼成增强 prompt → 交给 AI 回答
     */
    public Flux<String> chatWithKnowledge(String chatId, String question) {
        List<Content> contents = contentRetriever.retrieve(Query.from(question));
        String context = contents.stream()
                .map(Content::textSegment)
                .map(TextSegment::text)
                .collect(Collectors.joining("\n---\n"));

        log.debug("检索到 {} 条相关内容", contents.size());

        if (context.isEmpty()) {
            return consultantService.chatStream(chatId, question);
        }

        String augmentedPrompt = """
                请根据以下参考资料回答用户问题。如果参考资料不足，可以结合你的知识补充。

                【参考资料】
                %s

                【用户问题】
                %s
                """.formatted(context, question);

        return consultantService.chatStream(chatId, augmentedPrompt);
    }

    /**
     * 文档入库（简单文本）
     */
    public void ingest(String content) {
        TextSegment segment = TextSegment.from(content);
        Embedding embedding = embeddingModel.embed(segment).content();
        String id = embeddingStore.add(embedding, segment);
        log.info("文档入库完成, id: {}, 长度: {}", id, content.length());
    }

    /**
     * 文档入库（带元数据）
     */
    public void ingest(String content, String title, String source) {
        TextSegment segment = TextSegment.from(content,
                new dev.langchain4j.data.document.Metadata()
                        .put("title", title)
                        .put("source", source));
        Embedding embedding = embeddingModel.embed(segment).content();
        String id = embeddingStore.add(embedding, segment);
        log.info("文档入库完成, id: {}, title: {}, 长度: {}", id, title, content.length());
    }
}