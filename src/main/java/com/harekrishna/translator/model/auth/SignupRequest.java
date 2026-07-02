package com.harekrishna.translator.model.auth;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String confirmPassword;
}
