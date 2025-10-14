package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    private String email;         // email of user whose password we will change
    private String newPassword;   // new password
}
