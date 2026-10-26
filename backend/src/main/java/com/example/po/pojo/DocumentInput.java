package com.example.po.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档输入 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentInput {
    private String content;
    private String title;
    private String source;
}
