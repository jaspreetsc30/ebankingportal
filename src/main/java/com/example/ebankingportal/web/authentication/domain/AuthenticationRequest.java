package com.example.ebankingportal.web.authentication.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AuthenticationRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}
