package com.typingtutor.dto;

public class UserStatsDto {
    private String username;
    private int totalCompleted;
    private Double averageWpm;
    private int lessonsCompleted;

    public String getUsername() { return username; }
    public int getTotalCompleted() { return totalCompleted; }
    public Double getAverageWpm() { return averageWpm; }
    public int getLessonsCompleted() { return lessonsCompleted; }
    public void setUsername(String username) { this.username = username; }
    public void setTotalCompleted(int totalCompleted) { this.totalCompleted = totalCompleted; }
    public void setAverageWpm(Double averageWpm) { this.averageWpm = averageWpm; }
    public void setLessonsCompleted(int lessonsCompleted) { this.lessonsCompleted = lessonsCompleted; }
}
