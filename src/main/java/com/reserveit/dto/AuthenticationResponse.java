package com.reserveit.dto;

public class AuthenticationResponse {
    private String accessToken;
    private String role;
    private String companyId;

    public AuthenticationResponse(String accessToken, String role, String companyId) {
        this.accessToken = accessToken;
        this.role = role;
        this.companyId = companyId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
