package com.typingtutor.repository;

import com.typingtutor.entity.UserPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserPerformanceRepository extends JpaRepository<UserPerformance, Long> {

    List<UserPerformance> findByUserIdOrderByCompletedAtDesc(Long userId);

    // Returns ALL records for user+lesson ordered by wpm desc — take first in service
    @Query("SELECT p FROM UserPerformance p WHERE p.user.id = :userId AND p.lesson.id = :lessonId ORDER BY p.wpm DESC")
    List<UserPerformance> findAllByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    @Query("SELECT DISTINCT p.lesson.id FROM UserPerformance p WHERE p.user.id = :userId")
    List<Long> findCompletedLessonIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(p.wpm) FROM UserPerformance p WHERE p.user.id = :userId")
    Double findAverageWpmByUserId(@Param("userId") Long userId);
}
