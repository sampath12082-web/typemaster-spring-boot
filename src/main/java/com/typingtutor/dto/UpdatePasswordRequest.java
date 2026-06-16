package com.typingtutor.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePasswordRequest {

    // Both fields are RSA-OAEP encrypted (Base64) by the client — decrypted and
    // complexity-checked in AuthController; see PasswordCryptoService/PasswordPolicy.
    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
