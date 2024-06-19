package com.educationloan.PortalBackend.Controller;

import com.educationloan.PortalBackend.entity.User;
import com.educationloan.PortalBackend.entity.VerificationToken;
import com.educationloan.PortalBackend.event.RegistrationCompleteEvent;
import com.educationloan.PortalBackend.event.listener.RegistrationCompleteEventListener;
import com.educationloan.PortalBackend.payload.PasswordRequestUtil;
import com.educationloan.PortalBackend.payload.RegistrationDto;
import com.educationloan.PortalBackend.repository.VerificationTokenRepository;
import com.educationloan.PortalBackend.service.impl.PasswordResetTokenService;
import com.educationloan.PortalBackend.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private final UserServiceImpl userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;
    private final RegistrationCompleteEventListener eventListener;

    private final JavaMailSender mailSender;
    private final PasswordResetTokenService passwordResetTokenService;

    private final HttpServletRequest servletRequest;

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody RegistrationDto registrationRequest, final HttpServletRequest request) {
        User user = userService.registerUser(registrationRequest);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        String message = "Success! Please check your email to complete the registration process.";
        return ResponseEntity.ok(message);
    }

    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token){

        String link = applicationUrl(servletRequest)+ "/register/resend-verification-token?token="+token;

        String url = "http://localhost:3000/login";

        VerificationToken theToken = tokenRepository.findByToken(token);
        if (theToken.getUser().isEnabled()){
            return "This account has already been verified, please, login.";
        }
        String verificationResult = userService.validateToken(token);
        if (verificationResult.equalsIgnoreCase("valid")){
            return "Email verified successfully, <a href=\""+url+"\"> Click here to login.</a>";
        }
        return "Invalid verification link, <a href=\""+link+"\"> Get new verification link.</a>";
    }



    @GetMapping("/resend-verification-token")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          final HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User theUser = verificationToken.getUser();
        resendRegistrationVerificationTokenEmail(theUser, applicationUrl(request), verificationToken);
        return "A new verification link has been sent to your email," +
                " please, check to activate your account";
    }
    private void resendRegistrationVerificationTokenEmail(User theUser, String applicationUrl,
                                                          VerificationToken verificationToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationUrl+"/register/verifyEmail?token="+verificationToken.getToken();
        eventListener.sendVerificationEmail(url);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<String>  resetPasswordRequest(@RequestBody PasswordRequestUtil passwordRequestUtil,String applicationUrl,
                                       final HttpServletRequest servletRequest)
            throws MessagingException, UnsupportedEncodingException {

        Optional<User> user = userService.findByEmail(passwordRequestUtil.getEmail());
        if (user.isEmpty()){
            return ResponseEntity.ok("User not found");
        }
        String passwordResetToken = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetTokenForUser(user.get(), passwordResetToken);

        String url = "http://"+servletRequest.getServerName()+":"
                +servletRequest.getServerPort()+servletRequest.getContextPath()+"/register/reset-password?token="+passwordResetToken;
        try {
            String subject = "Password Reset Request ";
            String senderName = "Virtusa Education loan Portal Service";
            String mailContent = "<p> Hi,  </p>"+
                    "<p><b>You have recently requested to reset your password,</b>"+"" +
                    "Please, click the link below to complete the action.</p>"+
                    "<a href=\"" +url+ "\">Reset password</a>"+
                    "<p> Virtusa Education loan Portal Service";
            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);
            messageHelper.setFrom("virtusaeducationloanportal@gmail.com", senderName);
            messageHelper.setTo(passwordRequestUtil.getEmail());
            messageHelper.setSubject(subject);
            messageHelper.setText(mailContent, true);
            mailSender.send(message);
        }catch (MessagingException | UnsupportedEncodingException e) {

        }
        String message = "Please check your mail to reset the password.";
        return ResponseEntity.ok(message);
    }

    @PostMapping("/reset-password")
    public String resetPassword(HttpServletRequest request){

        String url = "http://localhost:3000/login";

        String theToken = request.getParameter("token");
        String password = request.getParameter("password");
        String tokenVerificationResult = passwordResetTokenService.validatePasswordResetToken(theToken);
        if (!tokenVerificationResult.equalsIgnoreCase("valid")){
            return "error in token";
        }
        Optional<User> theUser = passwordResetTokenService.findUserByPasswordResetToken(theToken);
        if (theUser.isPresent()){
            passwordResetTokenService.resetPassword(theUser.get(), password);
            return "Password changed successfully, <a href=\""+url+"\"> Click here to login.</a>";
        }
        return "Password link expired";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestBody PasswordRequestUtil requestUtil){
        User user = userService.findByEmail(requestUtil.getEmail()).get();
        if (!userService.oldPasswordIsValid(user, requestUtil.getOldPassword())){
            return "Incorrect old password";
        }
        userService.changePassword(user, requestUtil.getNewPassword());
        return "Password changed successfully";
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://"+request.getServerName()+":"
                +request.getServerPort()+request.getContextPath();
    }
}
