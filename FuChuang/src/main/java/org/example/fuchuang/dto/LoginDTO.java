package org.example.fuchuang.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String code;
    private String password;
}