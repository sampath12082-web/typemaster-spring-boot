package com.typingtutor.dto;

public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String createdAt;
    private int totalRuns;
    private double avgWpm;
    private double avgAccuracy;
    private int bestWpm;
    private boolean active = true;

    public AdminUserDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }
    public double getAvgWpm() { return avgWpm; }
    public void setAvgWpm(double avgWpm) { this.avgWpm = avgWpm; }
    public double getAvgAccuracy() { return avgAccuracy; }
    public void setAvgAccuracy(double avgAccuracy) { this.avgAccuracy = avgAccuracy; }
    public int getBestWpm() { return bestWpm; }
    public void setBestWpm(int bestWpm) { this.bestWpm = bestWpm; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
