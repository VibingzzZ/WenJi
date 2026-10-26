package com.example.controller;

import com.example.po.pojo.DocumentInput;
import com.example.service.aiservice.RagEnhancedChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * RAG 知识库管理接口
 * 用于非遗知识的入库和管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/rag")
@RequiredArgsConstructor
@Tag(name = "RAG 知识库管理", description = "非遗知识入库与管理")
public class RagKnowledgeController {

    private final RagEnhancedChatService ragChatService;

    /**
     * 单条知识入库
     */
    @Operation(summary = "单条知识入库", description = "将一条非遗知识存入向量数据库")
    @PostMapping("/ingest")
    public String ingest(
            @RequestParam String content,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String source) {
        
        try {
            if (title == null || title.isEmpty()) {
                title = "未命名知识";
            }
            if (source == null || source.isEmpty()) {
                source = "未知来源";
            }
            
            ragChatService.ingestDocument(content, title, source);
            return "知识入库成功";
        } catch (Exception e) {
            log.error("知识入库失败", e);
            return "知识入库失败";
        }
    }

    /**
     * 批量知识入库（示例数据）
     */
    @Operation(summary = "批量知识入库", description = "批量导入示例非遗知识")
    @PostMapping("/batch-ingest")
    public String batchIngest() {
        try {
            // 示例：批量导入非遗知识
            List<DocumentInput> documents = Arrays.asList(
                new DocumentInput(
                    "景泰蓝，又称'铜胎掐丝珐琅'，是一种在铜质的胎型上，用柔软的扁铜丝，掐成各种花纹焊上，然后把珐琅质的色釉填充在花纹内烧制而成的瓷器器物。因其在明朝景泰年间最为盛行，故后人称这种金属器为'景泰蓝'。景泰蓝正名'铜胎掐丝珐琅'，俗名'珐蓝'，又称'嵌珐琅'，是一种在铜质的胎型上，用柔软的扁铜丝，掐成各种花纹焊上，然后把珐琅质的色釉填充在花纹内烧制而成的瓷器器物。",
                    "景泰蓝制作技艺",
                    "国家级非物质文化遗产名录"
                ),
                new DocumentInput(
                    "青花瓷，又称白地青花瓷，常简称青花，是中国瓷器的主流品种之一，属釉下彩瓷。青花瓷是用含氧化钴的钴矿为原料，在陶瓷坯体上描绘纹饰，再罩上一层透明釉，经高温还原焰一次烧成。钴料烧成后呈蓝色，具有着色力强、发色鲜艳、烧成率高、呈色稳定的特点。原始青花瓷于唐宋已见端倪，成熟的青花瓷则出现在元代景德镇的湖田窑。明代青花成为瓷器的主流。清康熙时发展到了顶峰。",
                    "青花瓷烧制技艺",
                    "国家级非物质文化遗产名录"
                ),
                new DocumentInput(
                    "京剧，曾称平剧，中国五大戏曲剧种之一，场景布置注重写意，腔调以西皮、二黄为主，被视为中国国粹。京剧走遍世界各地，分布地以北京为中心，遍及中国，成为介绍、传播中国传统艺术文化的重要媒介。2010年11月16日，京剧被列入'人类非物质文化遗产代表作名录'。京剧的角色分为生、旦、净、丑四种行当，各有不同的表演特点和化妆风格。",
                    "京剧",
                    "联合国教科文组织人类非物质文化遗产"
                )
            );
            
            ragChatService.ingestDocuments(documents);
            return "批量入库成功，共导入 " + documents.size() + " 条知识";
        } catch (Exception e) {
            log.error("批量入库失败", e);
            return "批量入库失败: " + e.getMessage();
        }
    }

    /**
     * 从文件批量导入（待实现）
     */
    @Operation(summary = "从文件导入", description = "从 JSON/CSV 文件批量导入知识")
    @PostMapping("/import-from-file")
    public String importFromFile(@RequestParam String filePath) {
        // TODO: 实现从文件读取并批量导入
        return "功能开发中...";
    }
}
