package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.exception.BusinessException;
import com.example.mapper.TravelBlogMapper;
import com.example.po.pojo.TravelBlog;
import com.example.service.BadgeService;
import com.example.service.PointService;
import com.example.service.TravelBlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TravelBlogServiceImpl implements TravelBlogService {

    @Autowired
    private TravelBlogMapper travelBlogMapper;

    @Autowired
    private PointService pointService;

    @Autowired
    private BadgeService badgeService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发布旅游博客
     * <p>
     * 该方法处理博客发布的核心逻辑，包括分布式锁控制、博客入库、积分奖励及徽章发放。
     * 使用 Redis 分布式锁防止用户频繁操作，确保数据一致性。
     * </p>
     *
     * @param blog 待发布的旅游博客对象，包含用户信息及博客内容
     * @throws BusinessException 当获取锁失败（操作频繁）时抛出
     */
    @Override
    @Transactional
    public void publishBlog(TravelBlog blog) {

//        String lockKey = LOCK_PREFIX + "blog:" + blog.getUserId();
//        String lockValue = UUID.randomUUID().toString();
//
//        Boolean acquired = false;
//        try {
//            acquired = stringRedisTemplate.opsForValue()
//                    .setIfAbsent(lockKey, lockValue, BLOG_LOCK_EXPIRE, TimeUnit.SECONDS);
//
//            if (Boolean.FALSE.equals(acquired)) {
//                throw new BusinessException(429, "操作太频繁，请稍后再试");
//            }
//
//            int experience = 10;
//            blog.setExperienceEarned(experience);
//            travelBlogMapper.insert(blog);
//
//            pointService.addPoints(blog.getUserId(), experience, "blog", "发布旅游心得打卡");
//
//            badgeService.checkAndDistributeBadges(blog.getUserId(), "blog");
//            badgeService.checkAndDistributeBadges(blog.getUserId(), "footprint");
//        } finally {
//            // 只在当前线程持有锁的情况下释放锁
//            if (Boolean.TRUE.equals(acquired)) {
//                try {
//                    String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
//                    if (lockValue.equals(currentValue)) {
//                        stringRedisTemplate.delete(lockKey);
//                    }
//                } catch (Exception e) {
//                    // 释放锁失败不影响主业务流程，记录日志即可
//                }
//            }
//        }
    }

    @Override
    public List<TravelBlog> getUserBlogs(Integer userId) {
        return travelBlogMapper.selectList(
                Wrappers.<TravelBlog>lambdaQuery()
                        .eq(TravelBlog::getUserId, userId)
                        .orderByDesc(TravelBlog::getCreateTime)
        );
    }

    @Override
    @Transactional
    public void deleteBlog(Integer userId, Long blogId) {
        TravelBlog blog = travelBlogMapper.selectById(blogId);
        if (blog == null) {
            throw new BusinessException(404, "游记不存在");
        }
        if (!blog.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限删除该游记");
        }
        travelBlogMapper.deleteById(blogId);
    }
}
