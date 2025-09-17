package com.example.depressive.personalCenter.UserMessageRepository;
import com.example.depressive.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMessageRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
