package com.typingtutor.service;

import com.typingtutor.dto.ExamAttemptDto;
import com.typingtutor.dto.ExamDto;
import com.typingtutor.entity.*;
import com.typingtutor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock ExamRepository examRepository;
    @Mock ExamAttemptRepository examAttemptRepository;
    @Mock UserRepository userRepository;
    @Mock LessonRepository lessonRepository;
    @Mock UserPerformanceRepository performanceRepository;
    @Mock CertificateRepository certificateRepository;
    @Mock CertificateService certificateService;
    @Mock AuditLogService auditLogService;

    @InjectMocks ExamService examService;

    private User user;
    private Exam exam;
    private Lesson lesson;
    private UserPerformance passingPerf;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("student");
        user.setEmail("student@example.com");

        lesson = new Lesson();
        lesson.setId(17L);
        lesson.setDifficultyLevel(DifficultyLevel.ADVANCED);
        lesson.setDisplayOrder(17);
        lesson.setMinWpm(50);
        lesson.setMinAccuracy(85.0);
        lesson.setAiGenerated(false);
        lesson.setContentText("advanced content");

        exam = new Exam();
        exam.setId(3L);
        exam.setDifficultyLevel(DifficultyLevel.ADVANCED);
        exam.setDurationMinutes(60);
        exam.setMinWpm(55);
        exam.setMinAccuracy(90.0);
        exam.setContentText("exam content");
        exam.setActive(true);

        passingPerf = UserPerformance.builder()
                .user(user).lesson(lesson).wpm(60).accuracyPercentage(92.0).build();
    }

    @Test
    void getExamThrowsWhenTierNotComplete() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findByDifficultyLevelAndIsAiGeneratedFalseOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(lesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> examService.getExam("ADVANCED", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Complete all ADVANCED lessons");
    }

    @Test
    void getExamSucceedsWhenTierComplete() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findByDifficultyLevelAndIsAiGeneratedFalseOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(lesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of(passingPerf));
        when(examRepository.findByDifficultyLevelAndIsActiveTrue(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.of(exam));

        ExamDto dto = examService.getExam("ADVANCED", "student");

        assertThat(dto.getDurationMinutes()).isEqualTo(60);
        assertThat(dto.getMinWpm()).isEqualTo(55);
    }

    @Test
    void submitExamPassedIssuesCertificate() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findByDifficultyLevelAndIsAiGeneratedFalseOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(lesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of(passingPerf));
        when(examRepository.findByDifficultyLevelAndIsActiveTrue(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.of(exam));

        ExamAttempt savedAttempt = new ExamAttempt();
        savedAttempt.setId(99L);
        savedAttempt.setExam(exam);
        savedAttempt.setWpm(60);
        savedAttempt.setAccuracy(92.0);
        savedAttempt.setPassed(true);
        savedAttempt.setCompletedAt(LocalDateTime.now());
        savedAttempt.setStartedAt(LocalDateTime.now().minusSeconds(3600));
        when(examAttemptRepository.save(any())).thenReturn(savedAttempt);

        Certificate cert = new Certificate();
        cert.setCertificateId("test-uuid-123");
        cert.setUser(user);
        cert.setExamAttempt(savedAttempt);
        cert.setDifficultyLevel(DifficultyLevel.ADVANCED);
        when(certificateService.issueCertificate(any(), any())).thenReturn(cert);

        ExamAttemptDto result = examService.submitExam("ADVANCED", "student", 60, 92.0, 3600);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getCertificateId()).isEqualTo("test-uuid-123");
        verify(certificateService).issueCertificate(user, savedAttempt);
    }

    @Test
    void submitExamFailedDoesNotIssueCertificate() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findByDifficultyLevelAndIsAiGeneratedFalseOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(lesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of(passingPerf));
        when(examRepository.findByDifficultyLevelAndIsActiveTrue(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.of(exam));

        ExamAttempt failedAttempt = new ExamAttempt();
        failedAttempt.setId(100L);
        failedAttempt.setExam(exam);
        failedAttempt.setWpm(40);
        failedAttempt.setAccuracy(80.0);
        failedAttempt.setPassed(false);
        failedAttempt.setCompletedAt(LocalDateTime.now());
        failedAttempt.setStartedAt(LocalDateTime.now().minusSeconds(3600));
        when(examAttemptRepository.save(any())).thenReturn(failedAttempt);

        ExamAttemptDto result = examService.submitExam("ADVANCED", "student", 40, 80.0, 3600);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getCertificateId()).isNull();
        verify(certificateService, never()).issueCertificate(any(), any());
    }

    @Test
    void invalidTierThrowsIllegalArgument() {
        assertThatThrownBy(() -> examService.getExam("UNKNOWN", "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid tier");
    }
}
