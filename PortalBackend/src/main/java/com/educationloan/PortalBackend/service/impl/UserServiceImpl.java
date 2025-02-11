package com.educationloan.PortalBackend.service.impl;


import com.educationloan.PortalBackend.entity.User;
import com.educationloan.PortalBackend.entity.VerificationToken;
import com.educationloan.PortalBackend.exception.UserAlreadyExistsException;
import com.educationloan.PortalBackend.payload.RegistrationDto;
import com.educationloan.PortalBackend.repository.UserRepository;
import com.educationloan.PortalBackend.repository.VerificationTokenRepository;
import com.educationloan.PortalBackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegistrationDto request) {
        Optional<User> user = this.findByEmail(request.getEmail());
        if (user.isPresent()){
            throw new UserAlreadyExistsException(
                    "User with email "+request.getEmail() + " already exists");
        }
        var newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setAddress(request.getAddress());
        newUser.setRole(request.getRole());
        return userRepository.save(newUser);
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Override
    public void saveUserVerificationToken(User theUser, String token) {
        var verificationToken = new VerificationToken(token, theUser);
        tokenRepository.save(verificationToken);
    }

    @Override
    public String validateToken(String theToken) {
        VerificationToken token = tokenRepository.findByToken(theToken);
        if(token == null){
            return "Invalid verification token";
        }
        User user = token.getUser();
        Calendar calendar = Calendar.getInstance();
        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0){
//            tokenRepository.delete(token);
            return "Token already expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }
    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = tokenRepository.findByToken(oldToken);
        var verificationTokenTime = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpirationTime(verificationTokenTime.getTokenExpirationTime());
        return tokenRepository.save(verificationToken);
    }

    public void changePassword(User theUser, String newPassword) {
        theUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(theUser);
    }

//    @Override
//    public String validatePasswordResetToken(String token) {
//        return passwordResetTokenService.validatePasswordResetToken(token);
//    }
//
//    @Override
//    public User findUserByPasswordToken(String token) {
//        return passwordResetTokenService.findUserByPasswordToken(token).get();
//    }
//
//    @Override
//    public void createPasswordResetTokenForUser(User user, String passwordResetToken) {
//        passwordResetTokenService.createPasswordResetTokenForUser(user, passwordResetToken);
//    }
    @Override
    public boolean oldPasswordIsValid(User user, String oldPassword){
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}

