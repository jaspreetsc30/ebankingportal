package com.example.ebankingportal.web.authentication.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RegisterUserRequest {
    @NotBlank
    @Schema(name = "userName" ,description = "username to register with" , example = "JohnSmith")
    private String userName;
    @NotBlank
    @Schema(name = "password", description = "password to register with" , example = "password")
    private String password;
    @NotBlank
    @Schema(name = "iban", description = "unique IBAN of every user" , example = "89d3o179-abcd-465b-o9ee-e2d5f6ofEld46")
    private String iban;
}
