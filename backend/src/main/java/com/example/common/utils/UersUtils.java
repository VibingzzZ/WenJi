package com.example.common.utils;

import static com.example.common.constants.RedisConstants.USER_LOGIN_KEY;

import java.util.Map;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class UersUtils {
    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId(){
        Map<String,Object> claims = ThreadLocalUtil.get();
        if(claims==null){
            return null;
        }
        return (Long) claims.get("userId");
    }


    /**
     * 从请求中获取token
     * @param request 请求对象
     * @return token
     **/

    public static String getTokenFromRequest(HttpServletRequest request){
        String token = request.getHeader("Authorization");

        if(token == null){
            token = request.getHeader("authorization");
        }

        //处理Bearer前缀
        if(token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        return token != null ? token.trim() : null;
    }

    
    
    /**
     * 
     * @param request
     * @return
       */
    public static String getLoginKey() {
        ServletRequestAttributes requestAttributes = 
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            return USER_LOGIN_KEY + token;
        }
        return null;
    }
}
