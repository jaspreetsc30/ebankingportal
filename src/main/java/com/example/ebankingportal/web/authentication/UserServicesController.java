package com.example.ebankingportal.web.authentication;

import com.example.ebankingportal.web.authentication.domain.AuthenticationRequest;
import com.example.ebankingportal.web.authentication.domain.AuthenticationResponse;
import com.example.ebankingportal.web.authentication.domain.RegisterUserRequest;
import com.example.ebankingportal.services.UserService;
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

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RegisterUserRequest request){
        return userService.registerUser(request);
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request){
        return userService.authenticate(request);
    }
}
