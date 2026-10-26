package com.example.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;

    /**
     * 生成JWT令牌的方法
     * @param claims 包含在JWT中的声明信息，通常包含用户信息等
     * @return 返回生成的JWT令牌字符串
     */
    public static String generateToken(Map<String, Object> claims) {
        // 使用Jwts.builder()创建JWT构建器
        return Jwts.builder()
                // 添加声明信息
                .addClaims(claims)
                // 使用HS256算法和密钥对JWT进行签名
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                // 设置过期时间，当前时间加上过期时长
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                // compact()方法生成最终的JWT字符串
                .compact();
    }


    public static Claims parseToken(String token) throws Exception {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}