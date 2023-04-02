package com.example.ebankingportal.service;


import com.example.ebankingportal.web.authentication.domain.AuthenticationRequest;
import com.example.ebankingportal.web.authentication.domain.AuthenticationResponse;
import com.example.ebankingportal.web.authentication.domain.RegisterUserRequest;
import com.example.ebankingportal.model.User;
import com.example.ebankingportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final AuthenticationManager authenticationManager;
    public User getUser(String userName){
        return userRepository.findByUserName(userName);
    }
    public User getUserByIban(String iban){
        return userRepository.findByIBAN(iban);
    }


    public AuthenticationResponse registerUser(RegisterUserRequest request){
        if (getUser(request.getUserName())!=null)
            throw new DuplicateKeyException("The username already exists");
        if (getUserByIban(request.getIban())!=null)
            throw new DuplicateKeyException("The specified IBAN already exists");

        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword( passwordEncoder.encode(request.getPassword()));
        user.setIBAN(request.getIban());
        userRepository.save(user);
        Map<String, Object > Claim = new HashMap<>();
        Claim.put("IBAN" , request.getIban());
        String jwtToken = jwtService.generateToken(Claim, user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        String username = request.getUserName();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username,request.getPassword())
        );

        User user = getUser(username);
        if (user==null) throw new UsernameNotFoundException("Username does not exist");
        Map<String, Object > Claim = new HashMap<>();
        Claim.put("IBAN" , user.getIBAN());
        String jwtToken = jwtService.generateToken(Claim, user);
        return new AuthenticationResponse(jwtToken);
    }

}
