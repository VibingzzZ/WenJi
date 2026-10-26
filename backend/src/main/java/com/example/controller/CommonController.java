package com.example.controller;

import com.example.common.Result;
import com.example.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/common")
@Tag(name = "公共接口", description = "文件上传等公共接口")
public class CommonController {

    @Autowired
    private FileService fileService;

    /**
     * 文件上传接口
     * @param file 文件对象
     * @return Result<String> 文件URL
     */
    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件：{}", file.getOriginalFilename());
        try {
            String url = fileService.upload(file);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传异常", e);
            return Result.error("文件上传失败", null);
        }
    }
}
