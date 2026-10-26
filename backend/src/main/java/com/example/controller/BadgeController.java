package com.example.controller;

import com.example.common.Result;
import com.example.po.pojo.Badge;
import com.example.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.common.utils.UersUtils.getCurrentUserId;

@RestController
@RequestMapping("/badge")
@Tag(name = "徽章管理", description = "徽章展示接口")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    @Operation(summary = "获取我获得的徽章")
    @GetMapping("/myBadges")
    public Result<List<Badge>> getMyBadges() {
        Long userId = getCurrentUserId();
        if (userId == null) return Result.error("未登录");
        
        List<Badge> badges = badgeService.getUserBadges(userId.intValue());
        return Result.success(badges);
    }
}
