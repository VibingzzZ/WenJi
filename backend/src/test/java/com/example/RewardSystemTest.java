package com.example;

import com.example.common.Result;
import com.example.po.pojo.Badge;
import com.example.po.pojo.User;
import com.example.mapper.UserMapper;
import com.example.common.utils.JwtUtils;
import com.example.common.utils.Md5Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.USER_LOGIN_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 奖励系统测试类
 * 使用Spring Boot测试框架，配置自动MockMvc
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RewardSystemTest {

    @Autowired
    private MockMvc mockMvc;  // 用于模拟HTTP请求

    @Autowired
    private UserMapper userMapper;  // 用户数据访问层

    @Autowired
    private StringRedisTemplate stringRedisTemplate;  // Redis操作模板

    @Autowired
    private ObjectMapper objectMapper;  // JSON对象映射器

    /**
     * 测试获取用户徽章接口
     * @throws Exception 可能抛出的异常
     */
    @Test
    void testGetMyBadges() throws Exception {
        // 1. 创建测试用户并存入数据库
        String username = "u" + UUID.randomUUID().toString().substring(0, 8);  // 生成随机用户名
        User user = new User();
        user.setUserId((long) (Math.random() * 1000000 + 20000));  // 生成随机用户ID
        user.setUsername(username);
        user.setPassword(Md5Util.getMD5String("123456"));  // 密码MD5加密
        user.setPhone("139" + UUID.randomUUID().toString().substring(0, 8));  // 生成随机手机号
        user.setStatus("1");  // 设置用户状态为启用
        userMapper.insert(user);  // 插入用户数据到数据库

        // 2. 模拟登录并存入 Redis 缓存 (RefreshTokenInterceptor 逻辑)
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        String token = JwtUtils.generateToken(claims);
        
        String key = USER_LOGIN_KEY + token;
        Map<String, String> userMap = new HashMap<>();
        userMap.put("userId", user.getUserId().toString());
        userMap.put("username", user.getUsername());
        userMap.put("phone", user.getPhone());
        stringRedisTemplate.opsForHash().putAll(key, userMap);
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);

        // 3. 执行请求获取徽章
        MvcResult result = mockMvc.perform(get("/badge/myBadges")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Result<List<Badge>> response = objectMapper.readValue(content, new TypeReference<>() {});

        assertEquals(1, response.getCode());
        assertNotNull(response.getData());
        System.out.println("测试结果：成功获取用户徽章，数量：" + response.getData().size());

        // 4. 清理测试数据
        userMapper.deleteById(user.getUserId());
        stringRedisTemplate.delete(key);
    }
}
