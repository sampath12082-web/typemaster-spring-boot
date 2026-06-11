package com.typingtutor.dto;

public class ExamAttemptDto {
    private Long id;
    private String difficultyLevel;
    private int wpm;
    private double accuracy;
    private boolean passed;
    private String completedAt;
    private String certificateId;
    private int attemptsUsed;
    private int attemptsLeft;
    private boolean tierReset;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public int getWpm() { return wpm; }
    public void setWpm(int wpm) { this.wpm = wpm; }
    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    public int getAttemptsUsed() { return attemptsUsed; }
    public void setAttemptsUsed(int attemptsUsed) { this.attemptsUsed = attemptsUsed; }
    public int getAttemptsLeft() { return attemptsLeft; }
    public void setAttemptsLeft(int attemptsLeft) { this.attemptsLeft = attemptsLeft; }
    public boolean isTierReset() { return tierReset; }
    public void setTierReset(boolean tierReset) { this.tierReset = tierReset; }
}
