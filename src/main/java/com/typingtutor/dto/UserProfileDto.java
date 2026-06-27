package com.typingtutor.dto;

public class UserProfileDto {
    private String username;
    private Long userId;
    private String role;
    private String email;
    private String fullName;
    private String dateOfBirth;
    private boolean student;
    private String schoolName;
    private String classYear;
    private String courseSpecialization;
    private String occupation;
    private boolean placementCompleted;
    private String recommendedTier;
    private boolean emailVerified;
    private String emailVerificationDeadline;
    private Double averageWpm;
    private int lessonsCompleted;
    private int totalCompleted;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
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
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getEmailVerificationDeadline() { return emailVerificationDeadline; }
    public void setEmailVerificationDeadline(String emailVerificationDeadline) { this.emailVerificationDeadline = emailVerificationDeadline; }
    public Double getAverageWpm() { return averageWpm; }
    public void setAverageWpm(Double averageWpm) { this.averageWpm = averageWpm; }
    public int getLessonsCompleted() { return lessonsCompleted; }
    public void setLessonsCompleted(int lessonsCompleted) { this.lessonsCompleted = lessonsCompleted; }
    public int getTotalCompleted() { return totalCompleted; }
    public void setTotalCompleted(int totalCompleted) { this.totalCompleted = totalCompleted; }
}
