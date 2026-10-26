package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("image_recognition_log")
public class ImageRecognitionLog {
    private Long recognitionId;
    private String sessionId;        // 关联会话
    private Integer userId;          // 关联User.userId
    private String imageHash;        // 图片哈希(去重)
    private String siteId;           // 识别出的景点ID(关联HeritageSite)
    private BigDecimal confidence;   // 识别置信度, 0-1
    private String originalImageUrl; // 原始图片存储路径
    private String recognitionResult; // 识别结果详情(JSON)
    private LocalDateTime createTime; // 创建时间
}