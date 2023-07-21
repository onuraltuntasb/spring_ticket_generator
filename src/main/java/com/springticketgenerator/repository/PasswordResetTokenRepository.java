package com.springticketgenerator.repository;

import com.springticketgenerator.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
}
