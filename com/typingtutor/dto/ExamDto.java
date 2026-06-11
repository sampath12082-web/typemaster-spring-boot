package com.typingtutor.dto;

public class ExamDto {
    private Long id;
    private String difficultyLevel;
    private int durationMinutes;
    private int minWpm;
    private double minAccuracy;
    private String contentText;
    private int attemptCount;
    private int maxAttempts = 3;
    private int attemptsLeft;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getMinWpm() { return minWpm; }
    public void setMinWpm(int minWpm) { this.minWpm = minWpm; }
    public double getMinAccuracy() { return minAccuracy; }
    public void setMinAccuracy(double minAccuracy) { this.minAccuracy = minAccuracy; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public int getAttemptsLeft() { return attemptsLeft; }
    public void setAttemptsLeft(int attemptsLeft) { this.attemptsLeft = attemptsLeft; }
}
