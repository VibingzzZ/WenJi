package com.example.controller;

import com.example.common.Result;
import com.example.po.pojo.TravelBlog;
import com.example.service.TravelBlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.common.utils.UersUtils.getCurrentUserId;

@RestController
@RequestMapping("/travel")
@Tag(name = "旅游心得管理", description = "旅游心得打卡接口")
public class TravelBlogController {

    @Autowired
    private TravelBlogService travelBlogService;

    @Operation(summary = "发布旅游心得打卡")
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody TravelBlog blog) {
        Long userId = getCurrentUserId();
        if (userId == null) return Result.error("未登录");
        
        blog.setUserId(userId.intValue());
        travelBlogService.publishBlog(blog);
        return Result.success();
    }

    /**
     * 获取我的足迹 (博客列表)
     * 该接口用于获取当前登录用户的所有博客列表
     * @return 返回操作结果，包含博客列表数据或错误信息
     */
    @Operation(summary = "获取我的足迹 (博客列表)")
    @GetMapping("/myBlogs")
    public Result<List<TravelBlog>> getMyBlogs() {
        // 获取当前登录用户的ID
        Long userId = getCurrentUserId();
        // 如果用户未登录，返回错误信息
        if (userId == null) return Result.error("未登录");
        
        // 调用服务层方法获取用户的博客列表
        List<TravelBlog> blogs = travelBlogService.getUserBlogs(userId.intValue());
        // 返回成功结果，包含博客列表数据
        return Result.success(blogs);
    }

    @Operation(summary = "删除游记")
    @DeleteMapping("/delete/{blogId}")
    public Result<Void> deleteBlog(@PathVariable Long blogId) {
        Long userId = getCurrentUserId();
        if (userId == null) return Result.error("未登录");
        
        travelBlogService.deleteBlog(userId.intValue(), blogId);
        return Result.success();
    }
}
