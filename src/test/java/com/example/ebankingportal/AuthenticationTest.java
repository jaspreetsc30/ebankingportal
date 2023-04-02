package com.example.ebankingportal;

import com.example.ebankingportal.model.User;
import com.example.ebankingportal.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class AuthenticationTest {
    private JwtService jwtService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();


    }
    @Test
    public void doesJWTServiceWork() {
        User user = new User();
        user.setIBAN("unittest");
        user.setPassword("password");
        user.setUserName("unittest");
        HashMap<String,Object> claims = new HashMap<>();
        claims.put("IBAN" , user.getIBAN());

        String jwt = jwtService.generateToken(claims,user);
        Claims decryptedClaims = jwtService.extractAllClaims(jwt);
        assertEquals(jwtService.extractUsername(jwt), user.getUsername());
        assertTrue(claims.containsKey("IBAN") && claims.get("IBAN") == user.getIBAN());
    }

}