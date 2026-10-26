package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.Result;
import com.example.po.dto.LoginFormDTO;
import com.example.po.dto.UserUpdateDTO;
import com.example.mapper.UserMapper;
import com.example.po.pojo.User;
import com.example.service.UserService;
import com.example.common.utils.JwtUtils;
import com.example.common.utils.Md5Util;
import com.example.common.utils.RegexUtils;
import com.example.po.vo.UserInfoVO;
import com.example.po.vo.UserLoginVO;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.common.constants.RedisConstants.*;
import static com.example.common.Result.error;
import static com.example.common.utils.JwtUtils.generateToken;
import static com.example.common.utils.UersUtils.getLoginKey;


@Service
@Transactional
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    /**
     * 用户注册/登录
     * @param loginFormDTO 登录/注册信息
     * @return 登录结果
     */
    @Override
    public Result<?> login(@RequestBody LoginFormDTO loginFormDTO) {
        // 1. 判断用户名是否存在
        User user;
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", loginFormDTO.getUsername());
            user = userMapper.selectOne(queryWrapper);
            if(user!=null){
                return handlerLogin(user,loginFormDTO);
            }else{
                return handlerRegister(loginFormDTO);
            }
        } catch (Exception e) {
            log.error("认证处理异常",e);
            throw new RuntimeException("认证失败,请稍后重试");
        }
    }


    private Result<UserLoginVO>handlerLogin(User user,@RequestBody LoginFormDTO loginFormDTO){
        // 2.1 用户存在，验证密码和验证码
        String encryptedPassword = Md5Util.getMD5String(loginFormDTO.getPassword());

        // 验证验证码（需要从前端传入captchaKey）
        if (isCaptchaValid(loginFormDTO.getCaptchaKey(), loginFormDTO.getCaptcha())
                && encryptedPassword.equals(user.getPassword())) {

            // 生成JWT token
            Map<String, Object> claims = new HashMap<>();
            //把userIdlong类型转为字符串
            claims.put("userId", user.getUserId().toString());

            claims.put("username", user.getUsername());
            claims.put("phone",user.getPhone());
            String token = generateToken(claims);

            //将用户完整信息存入redis
            String key=USER_LOGIN_KEY+token;
            Map<String, Object> userInfo = getStringObjectMap(user);

            //存入redis
            stringRedisTemplate.opsForHash().putAll(key,userInfo);
            //设置过期时间
            stringRedisTemplate.expire(key, USER_LOGIN_EXPIRE, TimeUnit.MINUTES);

            UserLoginVO userLoginVO = new UserLoginVO();
            userLoginVO.setUserId(user.getUserId());
            userLoginVO.setAccessToken(token);
            userLoginVO.setLastLoginTime(LocalDateTime.now());

            log.info("用户登录成功：{}", user.getUsername());
            return Result.success(userLoginVO);
        } else {
            return error("密码错误或验证码错误，请重试", null);
        }
    }


    /**
     * 将用户信息转换为字符串类型的Map，用于存储到Redis中
     * @param user 用户信息
     * @return 包含用户信息的字符串类型的Map
     */
    @Nonnull
    private static Map<String, Object> getStringObjectMap(User user) {
        Map<String,Object>userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId().toString());
        //使用 putAll 这个方法时是会将所有 key 转变成 String，因为 userId 是 int 类型，需要转换成字符串
        //否则会导致
// java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')
        userInfo.put("username", user.getUsername());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("location", user.getLocation());
        userInfo.put("experience", user.getExperience() == null ? "0" : user.getExperience().toString());
        return userInfo;
    }

    /**
     * 验证注册信息
     * @param loginFormDTO 注册信息
     * @return 注册结果
     **/
    private Result<Void> handlerRegister(@RequestBody LoginFormDTO loginFormDTO){
        // 2.2 用户不存在，执行注册
        // 验证密码一致性
        if (loginFormDTO.getPassword()!=null&&!loginFormDTO.getPassword().equals(loginFormDTO.getRePassword())) {
            return error("密码为空或不一致", null);
        }

        // 验证手机号格式
        if (RegexUtils.isPhoneInvalid(loginFormDTO.getPhone())) {
            return error("手机号格式错误", null);
        }

        // 执行注册
        String encryptedPassword = null;
        if (loginFormDTO.getPassword() != null) {
            encryptedPassword = Md5Util.getMD5String(loginFormDTO.getPassword());
        }
        userMapper.register(loginFormDTO.getUsername(), loginFormDTO.getPhone(), encryptedPassword);

        log.info("用户注册成功：{}", loginFormDTO.getUsername());
        return Result.success(); // 注册成功返回空结果
    }

    /**
     * 验证验证码
     * @param captchaKey    图形验证特征码
     * @param userInput     用户输入的验证码
     * @return              验证结果
     */
    private boolean isCaptchaValid(String captchaKey, String userInput) {
        if(captchaKey==null||userInput==null){
            return false;
        }
        // 获取Redis中的验证码
        String redisCaptcha = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaKey);
        // 验证码不存在或过期
        if (redisCaptcha == null) {
            return false;
        }

        boolean isValid = redisCaptcha.equalsIgnoreCase(userInput);

        // 验证成功后删除验证码（一次性使用）
        if (isValid) {
            stringRedisTemplate.delete(CAPTCHA_PREFIX + captchaKey);
        }

        return isValid;
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public Result<UserInfoVO> getUserProfile(Long userId, String token) {
        try {
            // 从Redis中获取用户信息
            String key  = USER_LOGIN_KEY + userId;
            Map<Object,Object> userInfo = stringRedisTemplate.opsForHash().entries(key);
            if(!userInfo.isEmpty()){
                //Redis中有缓存，构建基础用户信息
                UserInfoVO userInfoVO = new UserInfoVO();
                userInfoVO.setUsername((String) userInfo.get("username"));
                userInfoVO.setPhone((String) userInfo.get("phone"));
                userInfoVO.setAvatarUrl((String) userInfo.get("avatarUrl"));
                userInfoVO.setLocation((String) userInfo.get("location"));
                Object expObj = userInfo.get("experience");
                if (expObj != null) {
                    userInfoVO.setExperience(Integer.parseInt(expObj.toString()));
                }
                log.info("从 redis 中获取用户信息成功，用户 ID：{}",userId);
                return Result.success(userInfoVO);
            }

            //redis没有缓存，从数据库获取完整信息
            UserInfoVO userInfoVO = userMapper.getByUserId(userId);
            log.info("从数据库中获取用户信息成功，用户ID：{}",userId);
            return Result.success(userInfoVO);
        } catch (Exception e) {
            log.error("获取用户信息异常",e);
            return Result.error("获取用户信息失败，请稍后重试",null);
        }
    }

    /**
     * 修改用户信息
     * @param userId 用户ID
     * @param userUpdateDTO 修改信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(Long userId, UserUpdateDTO userUpdateDTO) {
        // 处理密码加密等逻辑
        if (userUpdateDTO.getNewPassword() != null && !userUpdateDTO.getNewPassword().isEmpty()) {
            if (!userUpdateDTO.getNewPassword().equals(userUpdateDTO.getRePassword())) {
                throw new RuntimeException("两次密码输入不一致");
            }
            userUpdateDTO.setPassword(Md5Util.getMD5String(userUpdateDTO.getNewPassword()));
        }
        
        // 调用mapper更新用户信息
        userMapper.updateUserInfo(userId, userUpdateDTO);

        // 清理并更新 Redis 缓存：获取当前请求头中的 token
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String key=getLoginKey();
            if(key==null){
                throw new RuntimeException("获取登录缓存key失败");
            }
                
                // 查询最新用户信息
                User updatedUser = userMapper.selectById(userId);
                if (updatedUser != null) {
                    // 将最新用户信息更新回 Redis
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("userId", updatedUser.getUserId().toString());
                    userMap.put("username", updatedUser.getUsername());
                    userMap.put("avatarUrl", updatedUser.getAvatarUrl());
                    userMap.put("location", updatedUser.getLocation());
                    userMap.put("phone", updatedUser.getPhone());
                    userMap.put("experience", updatedUser.getExperience() == null ? "0" : updatedUser.getExperience().toString());
                    
                    // 这里注意：MyBatis-Plus 的 selectById 返回的是 User 对象
                    // 我们需要同步更新 Redis 中的哈希表
                    stringRedisTemplate.opsForHash().putAll(key, userMap);
                    // 重新设置过期时间
                    stringRedisTemplate.expire(key, USER_LOGIN_EXPIRE, TimeUnit.MINUTES);
                    
                    log.info("用户信息更新，已同步更新 Redis 缓存，Key: {}", key);
                }
            }
        }
    /**
     * 删除用户
     * @param userId 用户ID
     */
    @Override
    public void removeUserById(Long userId) {
        try {
            userMapper.deleteById(userId);
            log.info("用户删除成功，用户ID：{}",userId);
        } catch (RuntimeException e) {
            log.error("用户删除失败，用户ID：{}",userId,e);
            throw new RuntimeException(e);
        }catch (Exception e){
            log.error("删除用户发生位置错误，用户ID：{}",userId,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 用户登出
     * @param token 令牌
     * @return 登出结果
     */
    @Override
    public Result<Void> logout(String token) {
        try{
            Claims claims= JwtUtils.parseToken(token);
            String username= claims.get("username").toString();

            stringRedisTemplate.delete(USER_LOGIN_KEY+ token);
            log.info("用户登出成功，用户名:{}", username);
            return Result.success();
        } catch (Exception e) {
            log.error("用户登出失败，令牌解析异常", e);
            return Result.error("用户登出失败，令牌无效");
        }
    }

    /**
     * 根据用户Id查询用户信息
     * @param userId 用户Id
     * @return 用户信息
     */
    @Override
    public UserInfoVO getByUserId(Long userId) {
        return userMapper.getByUserId(userId);
    }
}
