package com.example.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问URL
     */
    String upload(MultipartFile file);
}
