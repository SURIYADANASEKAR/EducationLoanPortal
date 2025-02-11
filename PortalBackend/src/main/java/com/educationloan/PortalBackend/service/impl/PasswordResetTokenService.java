package com.educationloan.PortalBackend.service.impl;


import com.educationloan.PortalBackend.entity.PasswordResetToken;
import com.educationloan.PortalBackend.entity.User;
import com.educationloan.PortalBackend.repository.PasswordResetTokenRepository;
import com.educationloan.PortalBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public void createPasswordResetTokenForUser(User user, String passwordToken) {
        PasswordResetToken passwordRestToken = new PasswordResetToken(passwordToken, user);
        passwordResetTokenRepository.save(passwordRestToken);
    }

    public String validatePasswordResetToken(String passwordResetToken) {
        PasswordResetToken passwordToken = passwordResetTokenRepository.findByToken(passwordResetToken);
        if(passwordToken == null){
            return "Invalid verification token";
        }
       User user = passwordToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if ((passwordToken.getExpirationTime().getTime()-calendar.getTime().getTime())<= 0){
            return "Link expired";
        }
        return "valid";
    }
//    public Optional<User> findUserByPasswordToken(String passwordResetToken) {
//        return Optional.ofNullable(passwordResetTokenRepository.findByToken(passwordResetToken).getUser());
//    }
//
//    public PasswordResetToken findPasswordResetToken(String token){
//      return passwordResetTokenRepository.findByToken(token);
//    }
    public Optional<User> findUserByPasswordResetToken(String theToken) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(theToken).getUser());
    }
    public void resetPassword(User theUser, String newPassword) {
        theUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(theUser);
    }
}
