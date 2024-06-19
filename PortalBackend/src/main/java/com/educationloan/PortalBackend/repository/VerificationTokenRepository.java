package com.educationloan.PortalBackend.repository;

import com.educationloan.PortalBackend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;



public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
}
