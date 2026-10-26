package com.example.po.dto;

import lombok.Data;

@Data
public class SendVerifyCodeDTO {
    private String phone;
    private String type;
}
