package com.example.controller;

import com.example.common.Result;
import com.example.po.pojo.HeritageSite;
import com.example.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/map")
@Tag(name = "历史遗迹", description = " 历史遗迹相关接口")
public class HeritageController {
    @Autowired
    private SiteService siteService;

    @Operation(summary = "获取附近历史遗迹", description = "获取附近历史遗迹")
    @GetMapping("/initial")
    public Result<List<HeritageSite>> getNearby(@RequestParam Double lng, @RequestParam Double lat) {
        try {
            List<HeritageSite> sites = siteService.getNearbyHeritageSites(lng, lat);
            log.info("查询到 {} 个景点", sites != null ? sites.size() : 0);
            return Result.success(sites);
        } catch (Exception e) {
            log.error("查询景点失败: ", e);
            return Result.error("查询景点失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<HeritageSite> getSiteDetail(@PathVariable("id") String id) {
        HeritageSite detail = siteService.getDetail(id);
        if (detail != null) {
            return Result.success(detail);
        }
        return Result.error("未找到该景点的详细信息");
    }

}