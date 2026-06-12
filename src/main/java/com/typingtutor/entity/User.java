package com.typingtutor.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column
    private String email;

    // Feature C: enhanced profile
    @Column(name = "full_name", length = 100, columnDefinition = "VARCHAR(100) DEFAULT ''")
    private String fullName = "";

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "is_student", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean student = false;

    @Column(name = "school_name", length = 200)
    private String schoolName;

    @Column(name = "class_year", length = 50)
    private String classYear;

    @Column(name = "course_specialization", length = 200)
    private String courseSpecialization;

    @Column(name = "occupation", length = 200)
    private String occupation;

    // Feature B: placement
    @Column(name = "placement_completed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean placementCompleted = false;

    @Column(name = "recommended_tier", length = 20)
    private String recommendedTier;

    @Column(name = "placement_wpm")
    private Integer placementWpm;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    // Feature D: email verification
    @Column(name = "email_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailVerified = false;

    @Column(name = "password_changed", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean passwordChanged = true;

    @Column(name = "email_verification_deadline")
    private LocalDateTime emailVerificationDeadline;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public boolean isStudent() { return student; }
    public void setStudent(boolean student) { this.student = student; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getClassYear() { return classYear; }
    public void setClassYear(String classYear) { this.classYear = classYear; }
    public String getCourseSpecialization() { return courseSpecialization; }
    public void setCourseSpecialization(String courseSpecialization) { this.courseSpecialization = courseSpecialization; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public boolean isPlacementCompleted() { return placementCompleted; }
    public void setPlacementCompleted(boolean placementCompleted) { this.placementCompleted = placementCompleted; }
    public String getRecommendedTier() { return recommendedTier; }
    public void setRecommendedTier(String recommendedTier) { this.recommendedTier = recommendedTier; }
    public Integer getPlacementWpm() { return placementWpm; }
    public void setPlacementWpm(Integer placementWpm) { this.placementWpm = placementWpm; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public boolean isPasswordChanged() { return passwordChanged; }
    public void setPasswordChanged(boolean passwordChanged) { this.passwordChanged = passwordChanged; }
    public LocalDateTime getEmailVerificationDeadline() { return emailVerificationDeadline; }
    public void setEmailVerificationDeadline(LocalDateTime emailVerificationDeadline) { this.emailVerificationDeadline = emailVerificationDeadline; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username;
        private String password;
        private Role role = Role.USER;
        private String email;
        private String fullName = "";
        private LocalDate dateOfBirth;
        private boolean student = false;
        private String schoolName;
        private String classYear;
        private String courseSpecialization;
        private String occupation;
        private boolean active = true;
        private boolean emailVerified = false;
        private boolean passwordChanged = true;
        private boolean placementCompleted = false;

        public Builder username(String v) { this.username = v; return this; }
        public Builder password(String v) { this.password = v; return this; }
        public Builder role(Role v) { this.role = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder fullName(String v) { this.fullName = v; return this; }
        public Builder dateOfBirth(LocalDate v) { this.dateOfBirth = v; return this; }
        public Builder student(boolean v) { this.student = v; return this; }
        public Builder schoolName(String v) { this.schoolName = v; return this; }
        public Builder classYear(String v) { this.classYear = v; return this; }
        public Builder courseSpecialization(String v) { this.courseSpecialization = v; return this; }
        public Builder occupation(String v) { this.occupation = v; return this; }
        public Builder active(boolean v) { this.active = v; return this; }
        public Builder emailVerified(boolean v) { this.emailVerified = v; return this; }
        public Builder passwordChanged(boolean v) { this.passwordChanged = v; return this; }
        public Builder placementCompleted(boolean v) { this.placementCompleted = v; return this; }

        public User build() {
            User u = new User();
            u.username = username;
            u.password = password;
            u.role = role != null ? role : Role.USER;
            u.email = email;
            u.fullName = fullName != null ? fullName : "";
            u.dateOfBirth = dateOfBirth;
            u.student = student;
            u.schoolName = schoolName;
            u.classYear = classYear;
            u.courseSpecialization = courseSpecialization;
            u.occupation = occupation;
            u.active = active;
            u.emailVerified = emailVerified;
            u.passwordChanged = passwordChanged;
            u.placementCompleted = placementCompleted;
            return u;
        }
    }
}
