package com.typingtutor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ResendOtpRequest {
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "VERIFY_EMAIL|FIRST_LOGIN|RESET_PASSWORD") private String purpose;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}
