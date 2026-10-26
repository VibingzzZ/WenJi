package com.example.po.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema
public class UserInfoVO {
    @NotBlank
    private String username;// 用户名
    private Integer gender; // 性别
    private String password;// 密码
    private String phone;// 手机号
    private String realName;// 真实姓名
    @Email
    private String email;// 邮箱
    private String avatarUrl;// 头像
    private String location;// 常驻之地
    private String level;// 等级 见习学徒
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;// 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime; // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String lastLoginTime;// 最后登录时间
    private Integer isRealNameVerified;// 是否实名认证 0:未认证 1:已认证
    private Integer experience;// 经验 默认 0
}
