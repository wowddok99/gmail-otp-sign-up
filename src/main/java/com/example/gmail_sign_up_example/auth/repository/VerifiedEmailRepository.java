package com.example.gmail_sign_up_example.auth.repository;

import com.example.gmail_sign_up_example.auth.entity.VerifiedEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerifiedEmailRepository extends JpaRepository<VerifiedEmail, Long> {
    Optional<VerifiedEmail> findByEmail(String email);
    boolean existsByEmail(String email);
}