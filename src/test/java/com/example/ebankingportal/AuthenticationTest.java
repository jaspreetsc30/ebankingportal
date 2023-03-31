package com.example.ebankingportal;

import com.example.ebankingportal.models.User;
import com.example.ebankingportal.services.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTest {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mvc;


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
    @Test
    public void unauthenticatedUsersCannotAccessEndpoints() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v1/banking/inquire/123")).andExpect(status().isForbidden());
    }

}