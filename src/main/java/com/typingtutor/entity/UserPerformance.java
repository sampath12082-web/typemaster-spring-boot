package com.typingtutor.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_performance")
public class UserPerformance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private Integer wpm;

    @Column(name = "accuracy_percentage", nullable = false)
    private Double accuracyPercentage;

    @CreationTimestamp
    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;

    public UserPerformance() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private User user; private Lesson lesson; private Integer wpm; private Double accuracyPercentage;
        public Builder user(User v) { this.user = v; return this; }
        public Builder lesson(Lesson v) { this.lesson = v; return this; }
        public Builder wpm(Integer v) { this.wpm = v; return this; }
        public Builder accuracyPercentage(Double v) { this.accuracyPercentage = v; return this; }
        public UserPerformance build() {
            UserPerformance p = new UserPerformance();
            p.user = user; p.lesson = lesson; p.wpm = wpm; p.accuracyPercentage = accuracyPercentage;
            return p;
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Lesson getLesson() { return lesson; }
    public Integer getWpm() { return wpm; }
    public Double getAccuracyPercentage() { return accuracyPercentage; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setUser(User user) { this.user = user; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
    public void setWpm(Integer wpm) { this.wpm = wpm; }
    public void setAccuracyPercentage(Double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }
}
