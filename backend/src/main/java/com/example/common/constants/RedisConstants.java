package com.example.common.constants;


public class RedisConstants {
    public static final String CAPTCHA_PREFIX= "captcha:"; //验证码前缀
    
    public static final long CAPTCHA_EXPIRE =300;          //验证码过期时间
   
    public static final String USER_LOGIN_KEY = "user:";   //用户登录前缀
   
    public static final long USER_LOGIN_EXPIRE = 30;       //用户登录过期时间

    public static final String AI_CHAT_HISTORY_PREFIX="chat:history:";//ai对话前缀

    //RedisChatHistory.java
    public static final long AI_CHAT_HISTORY_EXPIRE=7 * 24 * 60 * 60;       //用户对话历史过期时间

    public static final  String CHAT_HISTORY_PREFIX = "chat:";  //用户存储对话前缀

    // 分布式锁前缀
    public static final String LOCK_PREFIX = "lock:";
    
    // 发布游记锁过期时间（秒）
    public static final long BLOG_LOCK_EXPIRE = 10;

    // 相似度阈值：高于此值认为 Redis 命中
    public static final double HIGH_SIMILARITY_THRESHOLD = 0.85;

    // 最低相似度阈值：低于此值的知识不用于增强
    public static final double MIN_SIMILARITY_THRESHOLD = 0.6;
}
