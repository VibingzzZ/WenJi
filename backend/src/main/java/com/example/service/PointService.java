package com.example.service;

public interface PointService {
    /**
     * 增加用户阅历
     * @param userId 用户 ID
     * @param points 变动阅历
     * @param source 来源 (blog/badge)
     * @param description 描述
     */
    /**
     * 增加阅历并记流水
     *
     * @param userId      用户 ID
     * @param points      变化的阅历值
     * @param source      来源 blog / badge / mall_exchange
     * @param description 描述文案
     * @param bizId       业务幂等键，形如 blog:123 / badge:5，必传且需稳定
     */
    void addPoints(Integer userId, Integer points, String source, String description, String bizId);
}
