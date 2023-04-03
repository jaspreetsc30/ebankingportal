package com.example.ebankingportal.web.authentication.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AuthenticationRequest {
    @NotBlank
    @Schema(name = "userName" ,description = "username to register with" , example = "JohnSmith")
    private String userName;
    @NotBlank
    @Schema(name = "password", description = "password to register with" , example = "password")
    private String password;
}
