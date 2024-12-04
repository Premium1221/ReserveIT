package com.reserveit.logic.interfaces;

public interface EmailService {
    void sendUserCredentials(String toEmail, String fullName, String password);
    void sendAdminNotification(String adminEmail, String createdUserName, String createdUserEmail);
    void sendPasswordReset(String toEmail, String resetToken);
}