package com.typingtutor.repository;

import com.typingtutor.entity.UserPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserPerformanceRepository extends JpaRepository<UserPerformance, Long> {

    // Used by PerformanceService + UserService
    List<UserPerformance> findByUserIdOrderByCompletedAtDesc(Long userId);

    // Used by AdminService
    List<UserPerformance> findByUserIdOrderByWpmDesc(Long userId);

    // Used by LessonService
    @Query("SELECT p FROM UserPerformance p WHERE p.user.id = :userId AND p.lesson.id = :lessonId ORDER BY p.wpm DESC")
    List<UserPerformance> findAllByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    // Used by UserService + LessonService
    @Query("SELECT DISTINCT p.lesson.id FROM UserPerformance p WHERE p.user.id = :userId")
    List<Long> findCompletedLessonIdsByUserId(@Param("userId") Long userId);

    // Used by UserService
    @Query("SELECT AVG(p.wpm) FROM UserPerformance p WHERE p.user.id = :userId")
    Double findAverageWpmByUserId(@Param("userId") Long userId);

    // Used by AdminService when deleting a user
    @Modifying
    @Transactional
    @Query("DELETE FROM UserPerformance p WHERE p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
