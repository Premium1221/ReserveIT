package com.reserveit.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage.RecipientType;

import java.io.InputStream;
import java.util.Properties;

@TestConfiguration
public class NoopMailTestConfig {

    @Bean
    @Primary
    public JavaMailSender noopJavaMailSender() {
        // No-op mail sender to avoid external SMTP in tests/CI
        return new JavaMailSender() {
            @Override public MimeMessage createMimeMessage() { return new MimeMessage(Session.getDefaultInstance(new Properties())); }
            @Override public MimeMessage createMimeMessage(InputStream contentStream) { try { return new MimeMessage(Session.getDefaultInstance(new Properties()), contentStream);} catch (Exception e){ throw new RuntimeException(e);} }
            @Override public void send(MimeMessage mimeMessage) throws MailException { /* no-op */ }
            @Override public void send(MimeMessage... mimeMessages) throws MailException { /* no-op */ }
            @Override public void send(SimpleMailMessage simpleMessage) throws MailException { /* no-op */ }
            @Override public void send(SimpleMailMessage... simpleMessages) throws MailException { /* no-op */ }
            @Override public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException { /* no-op */ }
            @Override public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException { /* no-op */ }
        };
    }
}
