package org.example.fuchuang.dto;

import lombok.Data;

@Data
public class ChangeDTO {
    private String email;
    private String password;
    private String newPassword;
    private String code;
    private String newNickName;
}
