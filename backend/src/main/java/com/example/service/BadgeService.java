package com.example.service;

import com.example.po.pojo.Badge;

import java.util.List;

public interface BadgeService {
    /**
     * 获取用户已获得的徽章
     * @param userId 用户ID
     * @return 徽章列表
     */
    List<Badge> getUserBadges(Integer userId);

    /**
     * 检查并分发徽章
     * @param userId 用户ID
     * @param type 触发类型 (blog/footprint)
     */
    void checkAndDistributeBadges(Integer userId, String type);
}
