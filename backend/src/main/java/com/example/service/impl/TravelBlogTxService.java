package com.example.service.impl;

import com.example.mapper.TravelBlogMapper;
import com.example.po.pojo.TravelBlog;
import com.example.service.BadgeService;
import com.example.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发布游记的事务模板。
 * <p>
 * 单独成为一个 Bean，是为了让 {@link org.springframework.transaction.annotation.Transactional}
 * 真正生效。若与 publishBlog 写在同一个类里用 this 调用，调用发生在原始对象内部、
 * 不经过 Spring 代理，事务拦截器没有机会执行，事务就是不存在的。
 */
@Service
@Slf4j
public class TravelBlogTxService {

    private static final int BLOG_EXPERIENCE = 10;

    @Autowired
    private TravelBlogMapper travelBlogMapper;

    @Autowired
    private PointService pointService;

    @Autowired
    private BadgeService badgeService;

    /**
     * 在一个事务内完成：游记入库、发阅历、检查徽章。
     * 任一步失败整体回滚，不会出现「游记有了但积分没发」这种半成品。
     *
     * @param blog        游记，调用前 userId 已填好
     * @param contentHash 正文 MD5，由外层算好传入
     */
    @Transactional(rollbackFor = Exception.class)
    public void doPublish(TravelBlog blog, String contentHash) {
        blog.setExperienceEarned(BLOG_EXPERIENCE);
        blog.setContentHash(contentHash);
        travelBlogMapper.insert(blog);

        // insert 之后 MP 会把自增主键回填到 blog.blogId，幂等键才拿得到
        Long blogId = blog.getBlogId();
        if (blogId == null) {
            throw new IllegalStateException("游记入库后未回填主键，无法生成积分幂等键");
        }

        pointService.addPoints(
                blog.getUserId(),
                BLOG_EXPERIENCE,
                "blog",
                "发布旅游博客",
                "blog:" + blogId
        );

        badgeService.checkAndDistributeBadges(blog.getUserId(), "blog");
        badgeService.checkAndDistributeBadges(blog.getUserId(), "footprint");
    }
}
