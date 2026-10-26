package com.example.service.impl;


import com.example.mapper.BadgeMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.mapper.TravelBlogMapper;
import com.example.mapper.UserBadgeMapper;
import com.example.po.pojo.Badge;
import com.example.po.pojo.TravelBlog;
import com.example.po.pojo.UserBadge;
import com.example.service.BadgeService;
import com.example.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BadgeServiceImpl implements BadgeService {

    @Autowired
    private BadgeMapper badgeMapper;

    @Autowired
    private UserBadgeMapper userBadgeMapper;

    @Autowired
    private TravelBlogMapper travelBlogMapper;

    @Autowired
    private PointService pointService;

    @Override
    public List<Badge> getUserBadges(Integer userId) {
        // 获取用户已获得的徽章ID
        List<UserBadge> userBadges = userBadgeMapper.selectList(
                Wrappers.<UserBadge>lambdaQuery().eq(UserBadge::getUserId, userId)
        );
        
        if (userBadges.isEmpty()) return List.of();
        
        Set<Integer> badgeIds = userBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toSet());
        
        // 查询徽章详情
        return badgeMapper.selectBatchIds(badgeIds);
    }

    @Override
    @Transactional
    public void checkAndDistributeBadges(Integer userId, String type) {
        // 1. 获取该类型下用户尚未获得的徽章
        List<UserBadge> earnedBadges = userBadgeMapper.selectList(
                Wrappers.<UserBadge>lambdaQuery().eq(UserBadge::getUserId, userId)
        );
        Set<Integer> earnedBadgeIds = earnedBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toSet());

        List<Badge> potentialBadges = badgeMapper.selectList(
                Wrappers.<Badge>lambdaQuery().eq(Badge::getType, type)
        );

        // 2. 根据不同类型统计进度
        long progress = 0;
        if ("blog".equals(type)) {
            progress = travelBlogMapper.selectCount(
                    Wrappers.<TravelBlog>lambdaQuery().eq(TravelBlog::getUserId, userId)
            );
        } else if ("footprint".equals(type)) {
            // 统计打卡过的不同景点数
            progress = travelBlogMapper.selectList(
                    Wrappers.<TravelBlog>lambdaQuery().eq(TravelBlog::getUserId, userId)
            ).stream().map(TravelBlog::getSiteId).distinct().count();
        }

        // 3. 循环检查是否满足发放条件
        for (Badge badge : potentialBadges) {
            if (!earnedBadgeIds.contains(badge.getBadgeId()) && progress >= badge.getThreshold()) {
                // 发放徽章
                userBadgeMapper.insert(UserBadge.builder()
                        .userId(userId)
                        .badgeId(badge.getBadgeId())
                        .build());
                
                // 增加奖励阅历
                if (badge.getExperienceBonus() > 0) {
                    pointService.addPoints(userId, badge.getExperienceBonus(), "badge", "获得徽章奖励：" + badge.getName());
                }
            }
        }
    }
}
