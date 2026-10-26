package com.example.po.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginFormDTO {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String rePassword;  // 移除@NotBlank注解，因为注册时才需要
    private String captcha;
    private String captchaKey;
}
