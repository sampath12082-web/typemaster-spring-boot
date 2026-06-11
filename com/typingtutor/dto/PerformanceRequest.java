package com.typingtutor.dto;

import jakarta.validation.constraints.*;

public class PerformanceRequest {
    @NotNull private Long lessonId;
    @NotNull @Min(0) @Max(300) private Integer wpm;
    @NotNull @Min(0) @Max(100) private Double accuracyPercentage;

    public Long getLessonId() { return lessonId; }
    public Integer getWpm() { return wpm; }
    public Double getAccuracyPercentage() { return accuracyPercentage; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public void setWpm(Integer wpm) { this.wpm = wpm; }
    public void setAccuracyPercentage(Double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }
}
