package com.example.ebankingportal.web.authentication;

import com.example.ebankingportal.web.authentication.domain.AuthenticationRequest;
import com.example.ebankingportal.web.authentication.domain.AuthenticationResponse;
import com.example.ebankingportal.web.authentication.domain.RegisterUserRequest;
import com.example.ebankingportal.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class UserServicesController {
    @Autowired
    private final UserService userService;

    @PostMapping("/registration")
    @Operation(summary = "registers as a user to the ebanking portal")
    public AuthenticationResponse register(@Valid @RequestBody RegisterUserRequest request){
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    @Operation(summary = "authenticate  to the ebanking portal and get a jwt token")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request){
        return userService.authenticate(request);
    }
}
