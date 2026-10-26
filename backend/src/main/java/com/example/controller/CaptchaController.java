package com.example.controller;

import com.example.common.Result;
import com.example.common.utils.CaptchaUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public Result<Map<String,String>>getCaptcha(){
        //生成验证码
        CaptchaUtils.CaptchaResult captchaResult = CaptchaUtils.generateCaptcha();

        //生成唯一标识
        String captchaKey = randomUUID().toString();
        log.info("验证码：{},验证标识{}",captchaResult.getCode(),captchaKey);
        //存到Redis缓存中
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaKey,
                captchaResult.getCode().toLowerCase(),
                CAPTCHA_EXPIRE, TimeUnit.MINUTES);

        //返回结果
        Map<String ,String> result=new HashMap<>();
        result.put("captchaKey",captchaKey);
        result.put("captchaImage","data:image/png;base64,"+captchaResult.getBase64Image());
        return Result.success(result);
    }
}
