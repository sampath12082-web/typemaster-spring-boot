package com.typingtutor.dto;

import com.typingtutor.entity.Inquiry;

public class InquiryDto {
    private Long id;
    private String username;
    private String subject;
    private String message;
    private String status;
    private String adminResponse;
    private String createdAt;

    public InquiryDto() {}

    public static InquiryDto from(Inquiry i) {
        InquiryDto dto = new InquiryDto();
        dto.setId(i.getId());
        dto.setUsername(i.getUser().getUsername());
        dto.setSubject(i.getSubject());
        dto.setMessage(i.getMessage());
        dto.setStatus(i.getStatus().name());
        dto.setAdminResponse(i.getAdminResponse());
        dto.setCreatedAt(i.getCreatedAt().toString());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
