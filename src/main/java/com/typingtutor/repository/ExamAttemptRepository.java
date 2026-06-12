package com.typingtutor.repository;

import com.typingtutor.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);

    @Query("SELECT a FROM ExamAttempt a WHERE a.user.id = :userId AND a.exam.id = :examId AND a.passed = true")
    List<ExamAttempt> findPassedByUserAndExam(@Param("userId") Long userId, @Param("examId") Long examId);

    @Query("SELECT COUNT(a) FROM ExamAttempt a WHERE a.user.id = :userId AND a.exam.id = :examId")
    long countByUserAndExam(@Param("userId") Long userId, @Param("examId") Long examId);

    @Query("SELECT COUNT(a) FROM ExamAttempt a WHERE a.user.id = :userId AND a.exam.id = :examId AND a.passed = true")
    long countPassedByUserAndExam(@Param("userId") Long userId, @Param("examId") Long examId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExamAttempt a WHERE a.user.id = :userId AND a.exam.id = :examId")
    void deleteByUserAndExam(@Param("userId") Long userId, @Param("examId") Long examId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExamAttempt a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
