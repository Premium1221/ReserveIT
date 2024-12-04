package com.reserveit.dto;

public class AuthenticationResponse {
    private String accessToken;
    private String role;

    public AuthenticationResponse(String accessToken, String role) {
        this.accessToken = accessToken;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRole() {
        return role;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRole(String role) {
        this.role = role;
    }
}