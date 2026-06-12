package com.typingtutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordRequest {
    @NotBlank private String changePasswordToken;
    @NotBlank @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
        message = "Password must be 8-100 characters with uppercase, lowercase, digit and special character (@$!%*?&)"
    ) private String newPassword;

    public String getChangePasswordToken() { return changePasswordToken; }
    public void setChangePasswordToken(String changePasswordToken) { this.changePasswordToken = changePasswordToken; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
