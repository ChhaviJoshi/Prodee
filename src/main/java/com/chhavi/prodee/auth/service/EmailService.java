package com.chhavi.prodee.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email to {} with token {} was not sent.", to, token);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@prodee.app");
            message.setTo(to);
            message.setSubject("Prodee - Password Reset Code");
            message.setText("Your password reset code is: " + token + "\n\nThis code will expire in 15 minutes.");
            
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
