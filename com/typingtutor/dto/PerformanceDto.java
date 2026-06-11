package com.typingtutor.dto;

import java.time.LocalDateTime;

public class PerformanceDto {
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private Integer wpm;
    private Double accuracyPercentage;
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public Long getLessonId() { return lessonId; }
    public String getLessonTitle() { return lessonTitle; }
    public Integer getWpm() { return wpm; }
    public Double getAccuracyPercentage() { return accuracyPercentage; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setId(Long id) { this.id = id; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
    public void setWpm(Integer wpm) { this.wpm = wpm; }
    public void setAccuracyPercentage(Double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
