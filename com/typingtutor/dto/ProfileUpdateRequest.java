package com.typingtutor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class ProfileUpdateRequest {
    @Size(max = 100) private String fullName;
    @Email @Size(max = 200) private String email;
    private LocalDate dateOfBirth;
    private Boolean student;
    private String schoolName;
    private String classYear;
    private String courseSpecialization;
    private String occupation;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Boolean getStudent() { return student; }
    public void setStudent(Boolean student) { this.student = student; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getClassYear() { return classYear; }
    public void setClassYear(String classYear) { this.classYear = classYear; }
    public String getCourseSpecialization() { return courseSpecialization; }
    public void setCourseSpecialization(String courseSpecialization) { this.courseSpecialization = courseSpecialization; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
}
