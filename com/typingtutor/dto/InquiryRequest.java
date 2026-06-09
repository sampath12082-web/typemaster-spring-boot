package com.typingtutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InquiryRequest {
    @NotBlank @Size(max = 200) private String subject;
    @NotBlank @Size(max = 2000) private String message;

    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public void setSubject(String v) { this.subject = v; }
    public void setMessage(String v) { this.message = v; }
}
