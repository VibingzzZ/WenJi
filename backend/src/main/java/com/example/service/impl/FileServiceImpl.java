package com.example.service.impl;

import com.example.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path:D:/develop/Projectes/WenJi/uploads/}")
    private String uploadPath;

    @Value("${file.access-url:/uploads/}")
    private String accessUrl;

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        // 创建目录
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        File dest = new File(uploadPath + fileName);
        try {
            file.transferTo(dest);
            log.info("文件上传成功：{}", dest.getAbsolutePath());
            return accessUrl + fileName;
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
