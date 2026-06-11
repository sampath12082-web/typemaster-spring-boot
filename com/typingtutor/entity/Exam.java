package com.typingtutor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private DifficultyLevel difficultyLevel;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "min_wpm", nullable = false)
    private int minWpm;

    @Column(name = "min_accuracy", nullable = false)
    private double minAccuracy;

    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    public Exam() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getMinWpm() { return minWpm; }
    public void setMinWpm(int minWpm) { this.minWpm = minWpm; }
    public double getMinAccuracy() { return minAccuracy; }
    public void setMinAccuracy(double minAccuracy) { this.minAccuracy = minAccuracy; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
