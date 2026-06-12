package com.typingtutor.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class RegisterRequest {
    @NotBlank @Size(min = 3, max = 50) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
        message = "Password must be 8-100 characters with uppercase, lowercase, digit and special character (@$!%*?&)"
    ) private String password;
    @NotBlank private String fullName;
    @NotNull private LocalDate dateOfBirth;
    private boolean student;
    private String schoolName;
    private String classYear;
    private String courseSpecialization;
    private String occupation;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
}
