package com.typingtutor.dto;

public class LessonWithStatusDto {
    private Long id;
    private String title;
    private String difficultyLevel;
    private String contentText;
    private int displayOrder;
    private int minWpm;
    private double minAccuracy;
    private String status; // LOCKED | AVAILABLE | PASSED | FAILED_ATTEMPT
    private Integer bestWpm;
    private Double bestAccuracy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public int getMinWpm() { return minWpm; }
    public void setMinWpm(int minWpm) { this.minWpm = minWpm; }
    public double getMinAccuracy() { return minAccuracy; }
    public void setMinAccuracy(double minAccuracy) { this.minAccuracy = minAccuracy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getBestWpm() { return bestWpm; }
    public void setBestWpm(Integer bestWpm) { this.bestWpm = bestWpm; }
    public Double getBestAccuracy() { return bestAccuracy; }
    public void setBestAccuracy(Double bestAccuracy) { this.bestAccuracy = bestAccuracy; }
}
