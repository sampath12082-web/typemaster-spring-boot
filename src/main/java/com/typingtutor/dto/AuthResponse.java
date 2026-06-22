package com.typingtutor.dto;

public class AuthResponse {
    private String token;
    private String username;
    private Long userId;
    private String role;
    private boolean emailVerified;
    private boolean placementCompleted;

    public AuthResponse(String token, String username, Long userId, String role,
                        boolean emailVerified, boolean placementCompleted) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.role = role;
        this.emailVerified = emailVerified;
        this.placementCompleted = placementCompleted;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public Long getUserId() { return userId; }
    public String getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean isPlacementCompleted() { return placementCompleted; }
}
