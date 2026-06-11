package com.typingtutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank private String changePasswordToken;
    @NotBlank @Size(min = 6, max = 100) private String newPassword;

    public String getChangePasswordToken() { return changePasswordToken; }
    public void setChangePasswordToken(String changePasswordToken) { this.changePasswordToken = changePasswordToken; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
