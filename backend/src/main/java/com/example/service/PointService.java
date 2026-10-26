package com.example.service;

public interface PointService {
    /**
     * 增加用户阅历
     * @param userId 用户 ID
     * @param points 变动阅历
     * @param source 来源 (blog/badge)
     * @param description 描述
     */
    void addPoints(Integer userId, Integer points, String source, String description);
}
