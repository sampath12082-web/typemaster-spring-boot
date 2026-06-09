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

    public Lesson() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public String getContentText() { return contentText; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
