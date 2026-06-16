package com.typingtutor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminCreateUserRequest {
    @NotBlank @Size(min = 3, max = 50) private String username;
    @Email private String email;
    // RSA-OAEP encrypted (Base64) by the client — see PasswordCryptoService/PasswordPolicy.
    @NotBlank private String password;

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setUsername(String v) { this.username = v; }
    public void setEmail(String v) { this.email = v; }
    public void setPassword(String v) { this.password = v; }
}
