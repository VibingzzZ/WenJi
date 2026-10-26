package com.example.service;

import com.example.common.Result;
import com.example.po.dto.LoginFormDTO;
import com.example.po.dto.UserUpdateDTO;
import com.example.po.vo.UserInfoVO;

public interface UserService {
    /* *
     * 用户登录
     * @Param loginDTO
     * @Return
     */
    Result<?> login(LoginFormDTO loginDTO);

    /* *
     * 获取用户信息
     * @Param userId
     * @Return
     */
    Result<UserInfoVO> getUserProfile(Long userId, String  token);

    /* *
     * 修改用户信息
     * @Param userId
     * @Return
     */
    void updateById(Long userId, UserUpdateDTO userUpdateDTO);

    /* *
     * 删除用户
     * @Param userId
     * @Return
     */
    void removeUserById(Long userId);

    /**
     * 用户登出
     * @return 登出结果
     */
    Result<Void> logout(String token);

    /* *
     * 根据用户ID获取用户信息
     * @Param userId
     * @Return
     */
    UserInfoVO getByUserId(Long userId);
}
