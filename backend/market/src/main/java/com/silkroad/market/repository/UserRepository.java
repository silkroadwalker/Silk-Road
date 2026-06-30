package com.silkroad.market.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.silkroad.market.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    Optional<User> findByUsername(String username);
}