package com.typingtutor.service;

import com.typingtutor.dto.ExamAttemptDto;
import com.typingtutor.dto.ExamDto;
import com.typingtutor.dto.ExamStatusDto;
import com.typingtutor.entity.*;
import com.typingtutor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);
    private static final int MAX_ATTEMPTS = 3;

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final UserPerformanceRepository performanceRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final AuditLogService auditLogService;

    public ExamService(ExamRepository examRepository,
                       ExamAttemptRepository examAttemptRepository,
                       UserRepository userRepository,
                       LessonRepository lessonRepository,
                       UserPerformanceRepository performanceRepository,
                       CertificateRepository certificateRepository,
                       CertificateService certificateService,
                       AuditLogService auditLogService) {
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.performanceRepository = performanceRepository;
        this.certificateRepository = certificateRepository;
        this.certificateService = certificateService;
        this.auditLogService = auditLogService;
    }

    public ExamDto getExam(String tier, String username) {
        DifficultyLevel level = parseTier(tier);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Tier completion must be checked first — gives a clear "complete lessons" message
        // before we even look up the exam
        verifyTierComplete(user.getId(), level);

        Exam exam = examRepository.findByDifficultyLevelAndIsActiveTrue(level)
                .orElseThrow(() -> new NoSuchElementException("No active exam for tier: " + tier));

        // Block if already passed
        if (examAttemptRepository.countPassedByUserAndExam(user.getId(), exam.getId()) > 0) {
            throw new IllegalArgumentException("You have already passed this exam and earned your certificate.");
        }

        // Block if all attempts exhausted
        long totalAttempts = examAttemptRepository.countByUserAndExam(user.getId(), exam.getId());

        ExamDto dto = new ExamDto();
        dto.setId(exam.getId());
        dto.setDifficultyLevel(exam.getDifficultyLevel().name());
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setMinWpm(exam.getMinWpm());
        dto.setMinAccuracy(exam.getMinAccuracy());
        dto.setContentText(exam.getContentText());
        dto.setAttemptCount((int) totalAttempts);
        dto.setMaxAttempts(MAX_ATTEMPTS);
        dto.setAttemptsLeft((int) (MAX_ATTEMPTS - totalAttempts));
        return dto;
    }

    @Transactional
    public ExamAttemptDto submitExam(String tier, String username, int wpm, double accuracy, int timeTaken) {
        DifficultyLevel level = parseTier(tier);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Exam exam = examRepository.findByDifficultyLevelAndIsActiveTrue(level)
                .orElseThrow(() -> new NoSuchElementException("No active exam for tier: " + tier));

        // Guard: already passed
        if (examAttemptRepository.countPassedByUserAndExam(user.getId(), exam.getId()) > 0) {
            throw new IllegalArgumentException("You have already passed this exam.");
        }

        // Guard: attempts exhausted
        long priorAttempts = examAttemptRepository.countByUserAndExam(user.getId(), exam.getId());
        if (priorAttempts >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("Maximum attempts reached. Please redo the lessons.");
        }

        verifyTierComplete(user.getId(), level);

        boolean passed = wpm >= exam.getMinWpm() && accuracy >= exam.getMinAccuracy() - 0.005;

        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setExam(exam);
        attempt.setWpm(wpm);
        attempt.setAccuracy(accuracy);
        attempt.setPassed(passed);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setStartedAt(LocalDateTime.now().minusSeconds(timeTaken));
        attempt = examAttemptRepository.save(attempt);

        int attemptsUsed = (int) priorAttempts + 1;
        int attemptsLeft = MAX_ATTEMPTS - attemptsUsed;

        log.info("Exam submitted: user={} tier={} wpm={} accuracy={} passed={} attempt={}/{}",
                username, tier, wpm, accuracy, passed, attemptsUsed, MAX_ATTEMPTS);

        ExamAttemptDto dto = toDto(attempt);
        dto.setAttemptsUsed(attemptsUsed);
        dto.setTierReset(false);

        auditLogService.log(username, "EXAM_SUBMITTED",
                "tier=" + tier + " wpm=" + wpm + " accuracy=" + accuracy + " passed=" + passed);

        if (passed) {
            Certificate cert = certificateService.issueCertificate(user, attempt);
            dto.setCertificateId(cert.getCertificateId());
            dto.setAttemptsLeft(0);
            auditLogService.log(username, "CERTIFICATE_ISSUED",
                    "tier=" + tier + " certId=" + cert.getCertificateId());
        } else if (attemptsLeft <= 0) {
            // 3rd failure — reset tier progress so user can start over
            List<Long> tierLessonIds = lessonRepository
                    .findByDifficultyLevelAndIsAiGeneratedFalseOrderByDisplayOrder(level)
                    .stream().map(Lesson::getId).collect(Collectors.toList());
            performanceRepository.deleteByUserIdAndLessonIdIn(user.getId(), tierLessonIds);
            examAttemptRepository.deleteByUserAndExam(user.getId(), exam.getId());
            dto.setTierReset(true);
            dto.setAttemptsUsed(0);
            dto.setAttemptsLeft(MAX_ATTEMPTS);
            log.info("Tier reset for user={} tier={} after {} failed attempts", username, tier, MAX_ATTEMPTS);
            auditLogService.log(username, "EXAM_TIER_RESET", "tier=" + tier);
        } else {
            dto.setAttemptsLeft(attemptsLeft);
        }

        return dto;
    }

    /** Returns per-tier exam status for the authenticated user. */
    public List<ExamStatusDto> getMyExamStatuses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        List<ExamStatusDto> statuses = new ArrayList<>();
        for (DifficultyLevel level : DifficultyLevel.values()) {
            Exam exam = examRepository.findByDifficultyLevelAndIsActiveTrue(level).orElse(null);
            if (exam == null) continue;

            long passedCount = examAttemptRepository.countPassedByUserAndExam(user.getId(), exam.getId());
            long totalAttempts = examAttemptRepository.countByUserAndExam(user.getId(), exam.getId());

            ExamStatusDto dto = new ExamStatusDto();
            dto.setTier(level.name());
            dto.setPassed(passedCount > 0);
            dto.setAttemptCount((int) totalAttempts);
            dto.setRemainingAttempts(passedCount > 0 ? 0 : (int) Math.max(0, MAX_ATTEMPTS - totalAttempts));

            // Attach certificate info if passed
            certificateRepository.findByUserIdOrderByIssuedAtDesc(user.getId()).stream()
                    .filter(c -> c.getDifficultyLevel() == level)
                    .findFirst()
                    .ifPresent(c -> {
                        dto.setCertificateId(c.getCertificateId());
                        dto.setIssuedAt(c.getIssuedAt().toString());
                        // Best passing attempt stats
                        examAttemptRepository.findPassedByUserAndExam(user.getId(), exam.getId()).stream()
                                .max(Comparator.comparingInt(ExamAttempt::getWpm))
                                .ifPresent(a -> {
                                    dto.setWpm(a.getWpm());
                                    dto.setAccuracy(a.getAccuracy());
                                });
                    });

            statuses.add(dto);
        }
        return statuses;
    }

    private void verifyTierComplete(Long userId, DifficultyLevel level) {
        List<Lesson> tierLessons = lessonRepository.findByDifficultyLevelAndIsAiGeneratedFalseOrderByDisplayOrder(level);
        List<UserPerformance> perfs = performanceRepository.findRecentByUserIdWithLesson(userId);

        long passedCount = tierLessons.stream()
                .filter(l -> perfs.stream().anyMatch(p ->
                        p.getLesson().getId().equals(l.getId())
                                && p.getWpm() >= l.getMinWpm()
                                && p.getAccuracyPercentage() >= l.getMinAccuracy()))
                .count();

        if (passedCount < tierLessons.size()) {
            throw new IllegalArgumentException(
                "Complete all " + level + " lessons before taking the exam. ("
                + passedCount + "/" + tierLessons.size() + " passed)");
        }
    }

    private ExamAttemptDto toDto(ExamAttempt attempt) {
        ExamAttemptDto dto = new ExamAttemptDto();
        dto.setId(attempt.getId());
        dto.setDifficultyLevel(attempt.getExam().getDifficultyLevel().name());
        dto.setWpm(attempt.getWpm());
        dto.setAccuracy(attempt.getAccuracy());
        dto.setPassed(attempt.isPassed());
        dto.setCompletedAt(attempt.getCompletedAt().toString());
        return dto;
    }

    private DifficultyLevel parseTier(String tier) {
        try {
            return DifficultyLevel.valueOf(tier.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid tier: " + tier + ". Use BASIC, INTERMEDIATE, or ADVANCED.");
        }
    }
}
