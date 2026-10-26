package com.example.controller;

import com.example.common.Result;
import com.example.po.dto.LoginFormDTO;
import com.example.po.dto.UserUpdateDTO;
import com.example.service.UserService;
import com.example.po.vo.UserInfoVO;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import static com.example.common.utils.UersUtils.getCurrentUserId;
import static com.example.common.utils.UersUtils.getTokenFromRequest;

@RestController
@Slf4j
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 用户登录/注册
     * @param loginFormDTO 登录信息
     * @return 登录结果
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginFormDTO loginFormDTO){
        return userService.login(loginFormDTO);
    }


    /**
     * 登出当前账号
     * @return 登出结果
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest  request){
        String token = getTokenFromRequest(request);
        if(StringUtils.isBlank(token)){
            return Result.error("未提供有效的令牌");
        }
        return userService.logout(token);
    }


    /**
     * 获取当前用户信息
     * @param request 请求对象
     * @return UserInfoVO 用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/currentUserInfo")
    public Result<UserInfoVO> getUserProfile(HttpServletRequest request){
        Long userId= getCurrentUserId();
        if(userId==null){
            return Result.error("未登录",null);
        }
        String token=getTokenFromRequest(request);
        return userService.getUserProfile(userId,token);
    }

    /**
     * 修改用户信息
     * @param userUpdateDTO 修改信息
     * @return UserInfoVO 修改后的用户信息
     */
    @Operation(summary = "修改用户信息")
    @PutMapping("/updateUserInfo")
    public Result<UserInfoVO> updateUserInfoById(@Validated @RequestBody UserUpdateDTO userUpdateDTO){
        Long userId=getCurrentUserId();
        if(userId==null){
            return Result.error("未登录",null);
        }
        log.info("修改用户信息：{}",userUpdateDTO);
        //执行更新
        userService.updateById(userId,userUpdateDTO);
        //重新查询最新数据
        UserInfoVO userInfoVO = userService.getByUserId(userId);
        return Result.success(userInfoVO);
    }

    /**
     * 注销用户信息
     * @return 删除用户信息
     */
    @Operation(summary = "注销用户信息")
    @DeleteMapping("/deleteUserInfo")
    public Result<Void> removeUserById(){
        Long userId=getCurrentUserId();
        if(userId==null){
            return Result.error("未登录",null);
        }
        userService.removeUserById(userId);
        return Result.success();
    }
}
