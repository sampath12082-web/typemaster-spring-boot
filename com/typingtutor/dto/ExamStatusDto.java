package com.typingtutor.dto;

public class ExamStatusDto {
    private String tier;
    private boolean passed;
    private int attemptCount;
    private int remainingAttempts;
    private String certificateId;
    private String issuedAt;
    private int wpm;
    private double accuracy;

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public int getRemainingAttempts() { return remainingAttempts; }
    public void setRemainingAttempts(int remainingAttempts) { this.remainingAttempts = remainingAttempts; }
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    public String getIssuedAt() { return issuedAt; }
    public void setIssuedAt(String issuedAt) { this.issuedAt = issuedAt; }
    public int getWpm() { return wpm; }
    public void setWpm(int wpm) { this.wpm = wpm; }
    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
}
