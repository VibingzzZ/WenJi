package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.exception.BusinessException;
import com.example.mapper.TravelBlogMapper;
import com.example.po.pojo.TravelBlog;
import com.example.service.TravelBlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class TravelBlogServiceImpl implements TravelBlogService {

    @Autowired
    private TravelBlogMapper travelBlogMapper;

    @Autowired
    private TravelBlogTxService travelBlogTxService;

    /**
     * 发布旅游博客。
     * <p>
     * 幂等策略：同一用户对同一正文只成功发布一次。
     * 先用 content_hash 查一次（挡住绝大多数重复点击），
     * 再由 travel_blog 的 uniq_user_content 唯一索引兜住并发撞车。
     * <p>
     * 注意事务边界：本方法<b>不加</b> @Transactional。
     * 幂等判断和异常捕获必须在事务之外，否则在事务内吞掉 DuplicateKeyException
     * 会让事务被标记 rollback-only，最终抛 UnexpectedRollbackException。
     * 真正的写库在 {@link TravelBlogTxService#doPublish} 里，那个类自己带事务。
     */
    @Override
    public void publishBlog(TravelBlog blog) {
        Integer userId = blog.getUserId();
        String content = blog.getContent();
        if (content == null || content.isBlank()) {
            throw new BusinessException(400, "游记内容不能为空");
        }

        String contentHash = DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));

        // 1. 幂等判断：同一用户 + 同一正文视为重复提交，直接当成功返回
        Long exists = travelBlogMapper.selectCount(
                Wrappers.<TravelBlog>lambdaQuery()
                        .eq(TravelBlog::getUserId, userId)
                        .eq(TravelBlog::getContentHash, contentHash)
        );
        if (exists != null && exists > 0) {
            log.info("重复提交已拦截 userId={}", userId);
            return;
        }

        // 2. 落库，并发撞车时由唯一索引拦截
        try {
            travelBlogTxService.doPublish(blog, contentHash);
        } catch (DuplicateKeyException e) {
            log.info("并发重复提交被唯一索引拦截 userId={}", userId);
        }
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
    @Transactional(rollbackFor = Exception.class)
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
