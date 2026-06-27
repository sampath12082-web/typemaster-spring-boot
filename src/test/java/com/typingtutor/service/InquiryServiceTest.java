package com.typingtutor.service;

import com.typingtutor.dto.InquiryDto;
import com.typingtutor.entity.Inquiry;
import com.typingtutor.entity.InquiryStatus;
import com.typingtutor.entity.User;
import com.typingtutor.repository.InquiryRepository;
import com.typingtutor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock InquiryRepository inquiryRepository;
    @Mock UserRepository userRepository;
    @Mock AuditLogService auditLogService;

    @InjectMocks InquiryService inquiryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("student");
        user.setEmail("student@example.com");
    }

    // ── submitInquiry ───────────────────────────────────────────────────────

    @Test
    void submitInquiry_createsInquiryAndReturnsDto() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(inv -> {
            Inquiry inq = inv.getArgument(0);
            inq.setId(10L);
            inq.setCreatedAt(LocalDateTime.of(2026, 6, 1, 9, 0));
            return inq;
        });

        InquiryDto result = inquiryService.submitInquiry("student", "Login issue", "Cannot log in");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("student");
        assertThat(result.getSubject()).isEqualTo("Login issue");
        assertThat(result.getMessage()).isEqualTo("Cannot log in");
        assertThat(result.getStatus()).isEqualTo("OPEN");
        assertThat(result.getPriority()).isEqualTo("HIGH");

        verify(auditLogService).log(eq("student"), eq("INQUIRY_SUBMITTED"), contains("Login issue"));
    }

    @Test
    void submitInquiry_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.submitInquiry("ghost", "Help", "Need help"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    // ── getMyInquiries ──────────────────────────────────────────────────────

    @Test
    void getMyInquiries_returnsUserInquiries() {
        Inquiry inq1 = buildInquiry(1L, "Subject A", "Message A", InquiryStatus.OPEN);
        Inquiry inq2 = buildInquiry(2L, "Subject B", "Message B", InquiryStatus.RESOLVED);

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(inquiryRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(inq1, inq2));

        List<InquiryDto> result = inquiryService.getMyInquiries("student");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSubject()).isEqualTo("Subject A");
        assertThat(result.get(0).getStatus()).isEqualTo("OPEN");
        assertThat(result.get(0).getPriority()).isEqualTo("HIGH");
        assertThat(result.get(1).getSubject()).isEqualTo("Subject B");
        assertThat(result.get(1).getStatus()).isEqualTo("RESOLVED");
        assertThat(result.get(1).getPriority()).isEqualTo("NORMAL");
    }

    @Test
    void getMyInquiries_returnsEmptyListWhenNone() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(inquiryRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<InquiryDto> result = inquiryService.getMyInquiries("student");

        assertThat(result).isEmpty();
    }

    // ── reopenInquiry ───────────────────────────────────────────────────────

    @Test
    void reopenInquiry_setsStatusReopenedAndIncrementsCount() {
        Inquiry inq = buildInquiry(5L, "Resolved Q", "Msg", InquiryStatus.RESOLVED);
        inq.setReopenCount(0);

        when(inquiryRepository.findById(5L)).thenReturn(Optional.of(inq));
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        InquiryDto result = inquiryService.reopenInquiry(5L, "student", "Still broken");

        assertThat(result.getStatus()).isEqualTo("REOPENED");
        assertThat(result.getReopenCount()).isEqualTo(1);
        assertThat(result.getReopenReason()).isEqualTo("Still broken");
        assertThat(result.getPriority()).isEqualTo("HIGH");

        verify(auditLogService).log(eq("student"), eq("INQUIRY_REOPENED"), contains("reopenCount=1"));
    }

    @Test
    void reopenInquiry_throwsWhenWrongUser() {
        Inquiry inq = buildInquiry(5L, "Subject", "Msg", InquiryStatus.RESOLVED);

        when(inquiryRepository.findById(5L)).thenReturn(Optional.of(inq));

        assertThatThrownBy(() -> inquiryService.reopenInquiry(5L, "otheruser", "reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("your own inquiries");
    }

    @Test
    void reopenInquiry_throwsWhenNotResolved() {
        Inquiry inq = buildInquiry(5L, "Subject", "Msg", InquiryStatus.OPEN);

        when(inquiryRepository.findById(5L)).thenReturn(Optional.of(inq));

        assertThatThrownBy(() -> inquiryService.reopenInquiry(5L, "student", "reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only resolved inquiries");
    }

    @Test
    void reopenInquiry_throwsWhenMaxReopensReached() {
        Inquiry inq = buildInquiry(5L, "Subject", "Msg", InquiryStatus.RESOLVED);
        inq.setReopenCount(3);

        when(inquiryRepository.findById(5L)).thenReturn(Optional.of(inq));

        assertThatThrownBy(() -> inquiryService.reopenInquiry(5L, "student", "reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum reopen limit");
    }

    @Test
    void reopenInquiry_throwsWhenInquiryNotFound() {
        when(inquiryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.reopenInquiry(999L, "student", "reason"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Inquiry not found");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Inquiry buildInquiry(Long id, String subject, String message, InquiryStatus status) {
        Inquiry inq = new Inquiry();
        inq.setId(id);
        inq.setUser(user);
        inq.setSubject(subject);
        inq.setMessage(message);
        inq.setStatus(status);
        inq.setCreatedAt(LocalDateTime.of(2026, 6, 1, 9, 0));
        return inq;
    }
}
