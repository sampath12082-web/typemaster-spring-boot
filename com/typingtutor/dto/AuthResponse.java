package com.typingtutor.dto;

public class AuthResponse {
    private String token;
    private String username;
    private Long userId;
    private String role;

    public AuthResponse(String token, String username, Long userId, String role) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public Long getUserId() { return userId; }
    public String getRole() { return role; }
}
