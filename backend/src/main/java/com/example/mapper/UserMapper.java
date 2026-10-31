package com.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.dto.UserUpdateDTO;
import com.example.po.pojo.User;
import com.example.po.vo.UserInfoVO;
import org.apache.ibatis.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 注册
     * @param username 用户名
     * @param phone    手机号
     * @param password 密码
     */
    @Insert("INSERT INTO user(username, password, phone, create_time, status) VALUES(#{username}, #{password}, #{phone}, NOW(), '1')")
    void register(@Param("username") String username, @Param("phone") String phone, @Param("password") String password);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return UserInfoVO
     **/
    @Select("select username,real_name,gender,phone,avatar_url,location,level,experience,is_real_name_verified,last_login_time,create_time,update_time from user where user_id=#{userId}")
    UserInfoVO getByUserId(@Param("userId") Long userId);

    /**
     * 更新用户最后登录时间
     * @param user 用户信息
     */
    @Update("update user set last_login_time=NOW() where user_id=#{userId}")
    void setUserLastLoginTime(User user);

    /**
     * 修改用户信息
     *
     * @param userId 用户ID
     * @param userUpdateDTO 修改的用户信息
     */
    void updateUserInfo(@Param("userId") Long userId, @Param("userUpdateDTO") @RequestBody @Validated UserUpdateDTO userUpdateDTO);

    /**
     * 增加经验
     * @param userId
     * @param points
     */
    @Update("UPDATE user set experience=IFNULL(experience,0)+#{points} where user_id = #{userId}")
    int addExperience(@Param("userId")Integer userId,@Param("points") Integer points);
}
