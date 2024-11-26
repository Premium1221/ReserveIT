package com.reserveit.dto;

public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String role;

    public AuthenticationResponse() {}

    public AuthenticationResponse(String accessToken, String role, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
