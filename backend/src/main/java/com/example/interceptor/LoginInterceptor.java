package com.example.interceptor;

import com.example.common.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器组件
 * 用于验证用户是否已登录，通过检查ThreadLocal中是否存在用户信息
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        if(ThreadLocalUtil.get()==null){
            return false;
        }
        return true;
    }
    
    /**
     * 请求处理完成后，清空ThreadLocal中的数据,防止内存泄露
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //请求处理完成后，清空ThreadLocal中的数据,防止内存泄露
        ThreadLocalUtil.remove();
    }
}
