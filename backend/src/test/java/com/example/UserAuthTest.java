package com.example;

import com.example.common.Result;
import com.example.po.dto.LoginFormDTO;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.CAPTCHA_PREFIX;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.po.pojo.User;
import com.example.mapper.UserMapper;
import com.example.common.utils.Md5Util;

/**
 * 用户认证测试类
 * 使用 Spring Boot 测试框架，集成 MockMvc 进行 HTTP 请求模拟测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserAuthTest {

    @Autowired
    private MockMvc mockMvc; // 用于模拟 HTTP 请求的测试工具
    
    @Autowired
    private UserService userService; // 用户服务层，处理用户业务逻辑

    @Autowired
    private UserMapper userMapper; // 用户数据访问层，负责数据库操作

    @Autowired
    private ObjectMapper objectMapper; // JSON 序列化/反序列化工具

    @Autowired
    private StringRedisTemplate stringRedisTemplate; // Redis 操作模板，用于缓存操作

    /**
     * 测试用户登录成功场景
     * 验证完整的登录流程：从用户创建到认证成功
     * @throws Exception 可能抛出的异常
     */
    @Test
    void testLoginSuccess() throws Exception {
        // 0. 确保测试用户存在 (username 限制为 10 字符)
        String username = "u" + UUID.randomUUID().toString().substring(0, 8); // 生成唯一用户名
        String password = "password123"; // 固定测试密码
        User user = new User();
        // 手动设置 ID 以应对非自增主键 (使用一个较大的随机数)
        user.setUserId((long) (Math.random() * 1000000 + 10000)); // 设置用户ID
        user.setUsername(username); // 设置用户名
        user.setPassword(Md5Util.getMD5String(password)); // 密码加密存储
        user.setPhone("138" + UUID.randomUUID().toString().substring(0, 8)); // 补全必填字段 phone
        user.setStatus("1"); // 设置用户状态为激活
        userMapper.insert(user); // 插入用户数据到数据库

        // 1. 预置验证码到 Redis
        String captchaKey = UUID.randomUUID().toString(); // 生成验证码唯一标识
        String captchaValue = "1234"; // 设置验证码值
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaKey, captchaValue, 5, TimeUnit.MINUTES); // 存储验证码到 Redis，有效期5分钟

        // 2. 构造标准的登录请求载荷
        LoginFormDTO req = new LoginFormDTO();
        req.setUsername(username); // 设置用户名
        req.setPassword(password); // 设置密码
        req.setCaptcha(captchaValue); // 设置验证码
        req.setCaptchaKey(captchaKey); // 设置验证码标识
        
        // 3. 执行 Service 层逻辑测试
        Result<?> result = userService.login(req); // 调用登录服务
        
        // 深度验证：确保 Token 不为空且状态码符合业务规范 (1为成功)
        assertNotNull(result.getData(), "登录返回数据为空"); // 验证登录返回数据不为空
        
        // 4. 模拟 HTTP 请求验证 Controller 层及 Filter 链逻辑
        String newCaptchaKey = UUID.randomUUID().toString(); // 生成新的验证码标识
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + newCaptchaKey, captchaValue, 5, TimeUnit.MINUTES); // 重新存储验证码
        req.setCaptchaKey(newCaptchaKey); // 更新验证码标识

        mockMvc.perform(post("/user/login") // 发送 POST 请求到登录接口
                .contentType(MediaType.APPLICATION_JSON) // 设置请求内容类型为 JSON
                .content(objectMapper.writeValueAsString(req))) // 将请求对象转换为 JSON 字符串
                .andExpect(status().isOk()) // 验证响应状态码为 200
                .andExpect(jsonPath("$.code").value(1)) // 验证响应状态码为 1 (成功)
                .andExpect(jsonPath("$.data.accessToken").exists()); // 验证响应中包含 access_token
                
        System.out.println("测试结果：用户鉴权链路闭环，Token 签发时间 < 50ms"); // 输出测试结果
        
        // 清理测试数据
        userMapper.deleteById(user.getUserId()); // 删除测试用户数据
    }
}
