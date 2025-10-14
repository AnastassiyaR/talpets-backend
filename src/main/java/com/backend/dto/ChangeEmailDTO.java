package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeEmailDTO {
    private String currentEmail;  // current email
    private String newEmail;      // new email
}
