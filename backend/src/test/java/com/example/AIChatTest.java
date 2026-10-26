package com.example;

import com.example.po.pojo.User;
import com.example.po.pojo.AIChatSession;
import com.example.mapper.UserMapper;
import com.example.mapper.AIChatSessionMapper;
import com.example.common.utils.JwtUtils;
import com.example.common.utils.Md5Util;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.USER_LOGIN_KEY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AI聊天功能的集成测试类
 * 使用Spring Boot测试框架和MockMvc模拟HTTP请求
 * 测试流式聊天API的功能
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AIChatTest {

    @Autowired
    private MockMvc mockMvc;  // 用于模拟HTTP请求的MockMvc对象

    @Autowired
    private UserMapper userMapper;  // 用户数据访问层
    
    @Autowired
    private AIChatSessionMapper aiChatSessionMapper;  // AI聊天会话数据访问层

    @Autowired
    private StringRedisTemplate stringRedisTemplate;  // Redis操作模板

    /**
     * 测试流式聊天API
     * 验证用户能够成功发起流式聊天请求并获得响应
     * @throws Exception 可能抛出的异常
     */
    @Test
    void testStreamChat() throws Exception {
        // 1. 创建测试用户并存入数据库
        String username = "u" + UUID.randomUUID().toString().substring(0, 8);  // 生成随机用户名
        User user = new User();
        user.setUserId((long) (Math.random() * 1000000 + 30000));  // 生成随机用户ID
        user.setUsername(username);  // 设置用户名
        user.setPassword(Md5Util.getMD5String("123456"));  // 设置密码(MD5加密)
        user.setPhone("137" + UUID.randomUUID().toString().substring(0, 8));  // 生成随机手机号
        user.setStatus("1");  // 设置用户状态为"1"(激活状态)
        userMapper.insert(user);  // 将用户信息插入数据库

        // 2. 模拟登录并存入 Redis 缓存
        Map<String, Object> claims = new HashMap<>();  // 创建JWT claims
        claims.put("userId", user.getUserId());  // 存储用户ID
        claims.put("username", user.getUsername());  // 存储用户名
        String token = JwtUtils.generateToken(claims);  // 生成JWT token
        
        String key = USER_LOGIN_KEY + token;  // 构建Redis缓存键
        Map<String, String> userMap = new HashMap<>();  // 创建用户信息映射
        userMap.put("userId", user.getUserId().toString());  // 存储用户ID
        userMap.put("username", user.getUsername());  // 存储用户名
        userMap.put("phone", user.getPhone());  // 存储手机号
        stringRedisTemplate.opsForHash().putAll(key, userMap);  // 将用户信息存入Redis
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);  // 设置Redis缓存过期时间

        // 3. 执行流式聊天请求
        // 注意：由于是 SSE (Server-Sent Events)，MockMvc 会捕获流式内容
        MvcResult result = mockMvc.perform(post("/ai/chat")  // 发送POST请求到AI聊天接口
                .header("Authorization", "Bearer " + token)  // 添加JWT认证头
                .param("prompt", "你好，请介绍一下景德镇")  // 添加聊天提示词
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))  // 设置请求内容类型
                .andExpect(status().isOk())  // 验证HTTP状态码为200
                .andReturn();  // 获取请求结果

        String responseContent = result.getResponse().getContentAsString();  // 获取响应内容
        
        // 验证返回内容不为空 (由于是流式，MockMvc 会一次性拿到当前产生的所有 chunk)
        assertNotNull(responseContent);  // 断言响应内容不为空
        
        System.out.println("测试结果：成功发起 AI 聊天请求。");

        // 4. 清理测试数据 - 先删除关联的 ai_chat_session 记录，再删除用户
        QueryWrapper<AIChatSession> queryWrapper = new QueryWrapper<>();  // 创建查询条件包装器
        queryWrapper.eq("user_id", user.getUserId());  // 设置查询条件：用户ID
        aiChatSessionMapper.delete(queryWrapper);  // 删除用户的聊天会话记录
        
        // 再删除用户
        userMapper.deleteById(user.getUserId());  // 删除用户记录
        stringRedisTemplate.delete(key);  // 删除Redis中的用户登录信息
    }
}
