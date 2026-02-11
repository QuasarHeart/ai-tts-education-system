package org.example.fuchuang.mapper;

import org.example.fuchuang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 只要方法名写对，JPA 自动生成 SQL：SELECT * FROM users WHERE email = ?
    User findByEmail(String email);
}