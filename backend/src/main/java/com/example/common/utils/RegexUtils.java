package com.example.common.utils;

import lombok.Data;

@Data
public class RegexUtils {
    public static boolean isPhoneInvalid(String phone){
        return phone != null && phone.matches("^1[3-9]\\d{8}$");
    }

    public static boolean isEmailInvalid(String email){
        return email != null && email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
    public static boolean isCaptchaInvalid(String captcha){
        return captcha != null && captcha.matches("^[a-zA-Z0-9]+$");
    }
}
