package com.educationloan.PortalBackend.Controller;

import com.educationloan.PortalBackend.entity.User;
import com.educationloan.PortalBackend.service.impl.PasswordResetTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;


@Controller
@RequestMapping("/register")
public class PasswordController {

    private final PasswordResetTokenService passwordResetTokenService;

    public PasswordController(PasswordResetTokenService passwordResetTokenService) {
        this.passwordResetTokenService = passwordResetTokenService;
    }


    @GetMapping("/reset-password")
    public String passwordResetFrom(@RequestParam("token") String token, Model model){
        model.addAttribute("token",token);
        return "password";
    }


}
