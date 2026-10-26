package com.example.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.utils.RedisChatHistoryUtils;
import com.example.mapper.AIChatSessionMapper;
import com.example.po.pojo.AIChatSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.example.common.utils.UersUtils.getCurrentUserId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于数据库的会话历史记录存储（数据库为主，Redis 为辅）
 * 数据库：存储所有会话元数据（主数据源）
 * Redis：仅作为缓存，加速会话列表查询
 */

@Slf4j
@Component
public class DatabaseChatHistoryRepository {

    @Autowired
    private  StringRedisTemplate redisTemplate;

    private final AIChatSessionMapper chatSessionMapper;
    
    // Redis 操作工具类（在构造函数中初始化）
    private final RedisChatHistoryUtils redisUtils;
    
    // 构造函数，在注入依赖后初始化 redisUtils
   public DatabaseChatHistoryRepository(StringRedisTemplate redisTemplate, 
                                        AIChatSessionMapper chatSessionMapper) {
        this.redisTemplate = redisTemplate;
        this.chatSessionMapper = chatSessionMapper;
        this.redisUtils = new RedisChatHistoryUtils(redisTemplate);
    }



    @Transactional(rollbackFor = Exception.class)
    public void save(String type, String sessionId, Long userId) {
        //最大重试次数和重试次数间间隔
        int maxRetries = 3;
        int retryCount = 0;
        //是否成功保存
        boolean success = false;
        
        while (!success && retryCount < maxRetries) {
            try {
                //1.先保存到数据库（主数据库）
                saveOrUpdateSession(sessionId,userId);
                //2.再更新Redis缓存（缓存会话ID列表）
                redisUtils.save(type, sessionId, userId);
                log.info("保存会话记录成功：userId：{}，sessionId：{}",userId,sessionId);
                success = true;
            } catch (Exception e) {
                retryCount++;
                log.error("保存会话记录失败，第 {} 次重试：{}", retryCount, e.getMessage());
                if (retryCount >= maxRetries) {
                    log.error("保存会话记录最终失败：userId：{}，sessionId：{}",userId,sessionId, e);
                    throw new RuntimeException("保存会话记录失败", e);
                }
                // 短暂休眠后重试
                try {
                    Thread.sleep(100 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 保存或更新会话到数据库（仅元数据）
     * @param sessionId 会话 ID (String类型,与数据库表结构匹配)
     * @param userId 用户 ID
     */
    private void saveOrUpdateSession(String sessionId, Long userId) {
        AIChatSession existingSession = getChatSessionBySessionId(sessionId);

        //判断该对话是否属于该用户
        if (existingSession != null && !existingSession.getUserId().equals(userId)) {
            log.error("会话归属权不匹配，sessionId: {}, 期望 userId: {}, 实际 userId: {}",
                    sessionId, existingSession.getUserId(), userId);
            throw new RuntimeException("无权操作该会话");
        }

        if (existingSession == null) {
            // 第一次创建会话
            AIChatSession newSession = AIChatSession.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .startTime(LocalDateTime.now())
                    .lastActiveTime(LocalDateTime.now())
                    .messageCount(0)
                    .status("active")
                    .build();

            chatSessionMapper.insert(newSession);
            log.info("创建新会话，sessionId: {}", sessionId);
        } else {
            // 更新已有会话（只更新活跃时间和消息数）
            AIChatSession updateSession = AIChatSession.builder()
                    .sessionId(existingSession.getSessionId())
                    .userId(existingSession.getUserId())  // 保持原有的 userId
                    .lastActiveTime(LocalDateTime.now())
                    .messageCount(existingSession.getMessageCount() + 1)
                    .status(existingSession.getStatus())
                    .currentLocation(existingSession.getCurrentLocation())
                    .sessionContext(existingSession.getSessionContext())
                    .build();
            //更新数据库
            chatSessionMapper.updateById(updateSession);
            log.debug("更新会话活跃时间，sessionId: {}", sessionId);
        }
    }

    /**
     * 根据 sessionId 查询会话信息
     * @param sessionId 会话 ID
     * @return 会话信息
     */
    private AIChatSession getChatSessionBySessionId(String sessionId) {
        // 查询数据库中是否存在该会话
        QueryWrapper<AIChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        return chatSessionMapper.selectOne(queryWrapper);
    }

    /**
     * 获取会话列表
     * @param type 会话类型
     * @return 会话列表
     */
    public List<String> getChatIds(String type) {
        // 获取当前用户ID
        Long userId = getCurrentUserId();
        
        // 如果用户未登录，返回空列表
        if (userId == null) {
            log.debug("用户未登录，返回空会话列表");
            return List.of();
        }
        
        //1.先从Redis缓存中读取
        List<String> cachedChatIds = redisUtils.getChatIds(type, userId);
        if(!cachedChatIds.isEmpty()){
            log.debug("从Redis缓存中获取会话列表成功：type: {}, 数量: {}", type, cachedChatIds.size());
            return cachedChatIds;
        }

        //2.redis中没有缓存，从数据库查

        log.debug("Redis中未命中，从数据库中获取会话列表：type: {}", type);
        QueryWrapper<AIChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "active")
                .eq("user_id", userId)
                .orderByDesc("last_active_time");

        List<AIChatSession>sessions = chatSessionMapper.selectList(queryWrapper);

        // 3. 转换为 sessionId 列表
        List<String> sessionIds = sessions.stream()
                .map(AIChatSession::getSessionId)
                .toList();

        // 4. 回填 Redis 缓存
        if (!sessionIds.isEmpty()) {
            log.debug("从数据库加载 {} 条会话，回填 Redis 缓存", sessionIds.size());
            for (String sessionId : sessionIds) {
                redisUtils.save(type, sessionId, userId);
            }
        }

        return sessionIds;
    }

    /**
     * 删除会话
     * 删除会话（先删数据库，再清理 Redis 缓存）
     * @param type 业务类型，如：chat、service、pdf
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String type, String sessionId, Long userId) {
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        
        while (!success && retryCount < maxRetries) {
            try {
                // 1. 验证权限并从数据库中删除
                AIChatSession session= getChatSessionBySessionId(sessionId);

                // 判断是否是该对话的拥有者操作，验证权限
                if (session == null || !session.getUserId().equals(userId)) {
                    throw new RuntimeException("无权删除会话");
                }

                // 2. 从数据库直接删除（物理删除）
                int deleted = chatSessionMapper.deleteById(session.getSessionId());
                if(deleted == 0){
                    throw new RuntimeException("数据删除异常");
                }

                // 3. 最后清理 Redis 缓存（即使失败也不影响数据库）
                try {
                    redisUtils.delete(type, sessionId, userId);
                } catch (Exception e) {
                    log.error("Redis 缓存清理失败，需要后续补偿清理，sessionId: {}", sessionId, e);
                }

                log.info("删除会话成功：sessionId:{}",sessionId);
                success = true;
            } catch (Exception e) {
                retryCount++;
                log.error("删除会话失败，第 {} 次重试：{}", retryCount, e.getMessage());
                if (retryCount >= maxRetries) {
                    log.error("删除会话最终失败：sessionId：{}", sessionId, e);
                    throw new RuntimeException("删除会话失败", e);
                }
                // 短暂休眠后重试
                try {
                    Thread.sleep(100 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
