package com.typingtutor.repository;

import com.typingtutor.entity.DifficultyLevel;
import com.typingtutor.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findByDifficultyLevelAndIsActiveTrue(DifficultyLevel level);
}
