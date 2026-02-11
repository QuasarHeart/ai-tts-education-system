package org.example.fuchuang.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String email;
    private String password;
    private String code; // 用户填写的验证码
}