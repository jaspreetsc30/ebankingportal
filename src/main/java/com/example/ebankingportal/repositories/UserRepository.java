package com.example.ebankingportal.repositories;

import com.example.ebankingportal.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUserName(String userName);
}
