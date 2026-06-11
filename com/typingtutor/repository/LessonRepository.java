package com.typingtutor.repository;

import com.typingtutor.entity.DifficultyLevel;
import com.typingtutor.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel level);
    List<Lesson> findAllByOrderByDifficultyLevelAscDisplayOrderAsc();

    List<Lesson> findByDifficultyLevelAndIsAiGeneratedFalseOrderByDisplayOrder(DifficultyLevel level);

    @Query("SELECT MAX(l.displayOrder) FROM Lesson l WHERE l.difficultyLevel = :level")
    Optional<Integer> findMaxDisplayOrderByDifficultyLevel(@Param("level") DifficultyLevel level);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.difficultyLevel = :level AND l.isAiGenerated = false")
    long countStandardLessonsByDifficultyLevel(@Param("level") DifficultyLevel level);
}
