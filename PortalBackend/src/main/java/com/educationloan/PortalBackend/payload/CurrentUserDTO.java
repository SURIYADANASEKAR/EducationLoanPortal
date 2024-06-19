package com.educationloan.PortalBackend.payload;

import com.educationloan.PortalBackend.entity.User;

public class CurrentUserDTO {

    private User user;

    public CurrentUserDTO(User user) {

        this.user = user;
    }
    public User getUser() {
        return user;
    }
}