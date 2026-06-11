package com.typingtutor.dto;

public class PlacementResultDto {
    private String recommendedTier;
    private Long startLessonId;
    private int wpm;
    private double accuracy;

    public PlacementResultDto() {}

    public PlacementResultDto(String recommendedTier, Long startLessonId, int wpm, double accuracy) {
        this.recommendedTier = recommendedTier;
        this.startLessonId = startLessonId;
        this.wpm = wpm;
        this.accuracy = accuracy;
    }

    public String getRecommendedTier() { return recommendedTier; }
    public void setRecommendedTier(String recommendedTier) { this.recommendedTier = recommendedTier; }
    public Long getStartLessonId() { return startLessonId; }
    public void setStartLessonId(Long startLessonId) { this.startLessonId = startLessonId; }
    public int getWpm() { return wpm; }
    public void setWpm(int wpm) { this.wpm = wpm; }
    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
}
