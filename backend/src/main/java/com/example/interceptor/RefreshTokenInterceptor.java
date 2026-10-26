package com.example.interceptor;

import com.example.po.pojo.User;
import com.example.common.utils.JwtUtils;
import com.example.common.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.USER_LOGIN_EXPIRE;
import static com.example.common.constants.RedisConstants.USER_LOGIN_KEY;

@Component
//@Component注解，将类注册为Spring组件，用于自动注入依赖
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate stringRedisTemplate;

    //构造函数
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 请求处理前拦截器
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器对象
     * @return 是否放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        if (uri.startsWith("/map/")) {
            return true;
        }
        
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        String token = authorization.substring(7).trim();
        try {
            JwtUtils.parseToken(token);
            String key = USER_LOGIN_KEY + token;
        
            Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        
            if (userMap.isEmpty()) {
                response.setStatus(401);
                return false;
            }
        
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.convertValue(userMap, User.class);
        
            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put("userId", user.getUserId());
            claimsMap.put("username", user.getUsername());
            claimsMap.put("phone", user.getPhone());
            ThreadLocalUtil.set(claimsMap);
        
            stringRedisTemplate.expire(key, USER_LOGIN_EXPIRE, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
