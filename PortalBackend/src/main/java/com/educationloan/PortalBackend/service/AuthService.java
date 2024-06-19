package com.educationloan.PortalBackend.service;


import com.educationloan.PortalBackend.payload.LoginDto;

public interface AuthService {
    String login(LoginDto loginDto);

}
