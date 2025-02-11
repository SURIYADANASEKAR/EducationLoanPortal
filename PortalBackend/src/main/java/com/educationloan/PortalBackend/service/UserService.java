package com.educationloan.PortalBackend.service;

import com.educationloan.PortalBackend.entity.User;
import com.educationloan.PortalBackend.entity.VerificationToken;
import com.educationloan.PortalBackend.payload.RegistrationDto;

import java.util.List;
import java.util.Optional;


public interface UserService {
    List<User> getUsers();

    User registerUser(RegistrationDto request);



    Optional<User> findByEmail(String email);

    void saveUserVerificationToken(User theUser, String verificationToken);

    String validateToken(String theToken);

    VerificationToken generateNewVerificationToken(String oldToken);

    void changePassword(User theUser, String newPassword);

//    String validatePasswordResetToken(String token);
//
//    User findUserByPasswordToken(String token);
//
//    void createPasswordResetTokenForUser(User user, String passwordResetToken);

    boolean oldPasswordIsValid(User user, String oldPassword);
}
