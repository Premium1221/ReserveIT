package com.reserveit.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 12;
    private final SecureRandom random = new SecureRandom();

    public String generateSecurePassword() {
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each required character type
        password.append(CHARS.charAt(random.nextInt(26))); // Uppercase
        password.append(CHARS.charAt(26 + random.nextInt(26))); // Lowercase
        password.append(CHARS.charAt(52 + random.nextInt(10))); // Number
        password.append(CHARS.charAt(62 + random.nextInt(8))); // Special char

        // Fill the rest randomly
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}