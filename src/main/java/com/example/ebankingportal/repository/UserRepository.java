package com.example.ebankingportal.repository;

import com.example.ebankingportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUserName(String userName);
    User findByIBAN(String iban);

}
