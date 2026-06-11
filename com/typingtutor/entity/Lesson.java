package com.typingtutor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "content_text", nullable = false, length = 2000)
    private String contentText;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "min_wpm", nullable = false, columnDefinition = "INTEGER DEFAULT 20")
    private int minWpm = 20;

    @Column(name = "min_accuracy", nullable = false, columnDefinition = "DOUBLE DEFAULT 85.0")
    private double minAccuracy = 85.0;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @Column(name = "is_ai_generated", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isAiGenerated = false;

    @Column(name = "generated_for_user_id")
    private Long generatedForUserId;

    public Lesson() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public String getContentText() { return contentText; }
    public Integer getDisplayOrder() { return displayOrder; }
    public int getMinWpm() { return minWpm; }
    public double getMinAccuracy() { return minAccuracy; }
    public boolean isActive() { return isActive; }
    public boolean isAiGenerated() { return isAiGenerated; }
    public Long getGeneratedForUserId() { return generatedForUserId; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public void setMinWpm(int minWpm) { this.minWpm = minWpm; }
    public void setMinAccuracy(double minAccuracy) { this.minAccuracy = minAccuracy; }
    public void setActive(boolean active) { isActive = active; }
    public void setAiGenerated(boolean aiGenerated) { isAiGenerated = aiGenerated; }
    public void setGeneratedForUserId(Long generatedForUserId) { this.generatedForUserId = generatedForUserId; }
}
