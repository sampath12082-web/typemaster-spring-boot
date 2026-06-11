package com.typingtutor.dto;

import com.typingtutor.entity.DifficultyLevel;

public class LessonDto {
    private Long id;
    private String title;
    private DifficultyLevel difficultyLevel;
    private String contentText;
    private Integer displayOrder;
    private Integer bestWpm;
    private Double bestAccuracy;
    private boolean completed;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public String getContentText() { return contentText; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Integer getBestWpm() { return bestWpm; }
    public Double getBestAccuracy() { return bestAccuracy; }
    public boolean isCompleted() { return completed; }
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public void setBestWpm(Integer bestWpm) { this.bestWpm = bestWpm; }
    public void setBestAccuracy(Double bestAccuracy) { this.bestAccuracy = bestAccuracy; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
