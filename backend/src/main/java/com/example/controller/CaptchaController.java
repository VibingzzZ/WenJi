package com.example.controller;

import com.example.common.Result;
import com.example.common.utils.CaptchaUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.CAPTCHA_EXPIRE;
import static com.example.common.constants.RedisConstants.CAPTCHA_LIMIT_EXPIRE;
import static com.example.common.constants.RedisConstants.CAPTCHA_LIMIT_PREFIX;
import static com.example.common.constants.RedisConstants.CAPTCHA_PREFIX;
import static java.util.UUID.randomUUID;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "验证码管理", description = "验证码相关接口")
public class CaptchaController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取图形验证码
     * @return 验证码
     */
    @Operation(summary = "获取图形验证码", description = "生成并返回图形验证码，包含验证码图片和验证码标识")
    @GetMapping("/captcha")
    public Result<Map<String,String>>getCaptcha(HttpServletRequest request){
        // 1. 先限流，再生成图片
        // 生成图片是 CPU 密集操作，被限流时不应该白白消耗一次
        String limitKey = CAPTCHA_LIMIT_PREFIX + getClientIp(request);
        boolean allowed = Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                limitKey, "1", CAPTCHA_LIMIT_EXPIRE, TimeUnit.SECONDS));
        if (!allowed) {
            log.warn("验证码请求过于频繁，key：{}", limitKey);
            return Result.error("请求过于频繁，请等待几秒后重试");
        }

        // 2. 生成验证码
        CaptchaUtils.CaptchaResult captchaResult = CaptchaUtils.generateCaptcha();

        // 3. 只返回纯 uuid，前缀由后端拼接，避免前端回传时重复拼前缀
        String captchaId = randomUUID().toString();

        // 注意：验证码存储（5 分钟）和限流（2 秒）是两个不同生命周期，用两个 key
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaId,
                captchaResult.getCode().toLowerCase(),
                CAPTCHA_EXPIRE, TimeUnit.MINUTES);

        // 不要把验证码明文写进日志，日志泄露等于验证码失效
        log.debug("生成验证码，标识：{}", captchaId);

        //返回结果
        Map<String ,String> result=new HashMap<>();
        result.put("captchaKey",captchaId);
        result.put("captchaImage","data:image/png;base64,"+captchaResult.getBase64Image());
        return Result.success(result);
    }

    /**
     * 取客户端 IP 作为限流维度。
     *
     * <p>不能用 sessionId：攻击者每次请求不带 cookie 就是新 session，限流形同虚设，
     * 而且会不断堆积 Tomcat 的 Session 对象。
     *
     * <p>如果以后部署在 Nginx 后面，这里要改成取 {@code X-Forwarded-For} 的首段，
     * 但前提是 Nginx 配置了可信转发，否则该请求头可以被客户端伪造。
     */
    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
