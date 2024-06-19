package com.educationloan.PortalBackend.payload;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDto
{
    String firstName;
    String lastName;
    String email;
    String password;
    String phoneNumber;
    String address;
    String role;
}


