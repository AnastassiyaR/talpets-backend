package com.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSignupDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
