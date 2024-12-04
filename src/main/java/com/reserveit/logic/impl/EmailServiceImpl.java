package com.reserveit.logic.impl;

import com.reserveit.logic.interfaces.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final String fromEmail = "areserveit@gmail.com";

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendUserCredentials(String toEmail, String fullName, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your ReserveIT Account Credentials");
        message.setText(String.format("""
            Hello %s,
            
            Your ReserveIT account has been created successfully.
            
            Email: %s
            Password: %s
            
            Please change your password after your first login.
            
            Best regards,
            ReserveIT Team
            """, fullName, toEmail, password));

        mailSender.send(message);
    }

    @Override
    public void sendAdminNotification(String adminEmail, String createdUserName, String createdUserEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject("New User Account Created");
        message.setText(String.format("""
            Hello Admin,
            
            A new user account has been created :
            
            Name: %s
            Email: %s
            
            Best regards,
            ReserveIT System
            """, createdUserName, createdUserEmail));

        mailSender.send(message);
    }

    @Override
    public void sendPasswordReset(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("ReserveIT Password Reset");
        message.setText(String.format("""
            Hello,
            
            A password reset was requested for your ReserveIT account.
            
            Your reset token is: %s
            
            If you didn't request this, please ignore this email.
            
            Best regards,
            ReserveIT Team
            """, resetToken));

        mailSender.send(message);
    }

    // Helper method for reusability
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}