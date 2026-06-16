package com.typingtutor.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {
    @NotBlank private String changePasswordToken;
    // RSA-OAEP encrypted (Base64) by the client — see PasswordCryptoService/PasswordPolicy.
    @NotBlank private String newPassword;

    public String getChangePasswordToken() { return changePasswordToken; }
    public void setChangePasswordToken(String changePasswordToken) { this.changePasswordToken = changePasswordToken; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
