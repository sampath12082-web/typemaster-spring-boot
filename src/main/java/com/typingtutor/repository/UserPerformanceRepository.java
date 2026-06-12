package com.typingtutor.repository;

import com.typingtutor.entity.UserPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserPerformanceRepository extends JpaRepository<UserPerformance, Long> {

    List<UserPerformance> findByUserIdOrderByCompletedAtDesc(Long userId);
    List<UserPerformance> findByUserIdOrderByWpmDesc(Long userId);
    boolean existsByUserId(Long userId);

    @Query("SELECT p FROM UserPerformance p WHERE p.user.id = :userId AND p.lesson.id = :lessonId ORDER BY p.wpm DESC")
    List<UserPerformance> findAllByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    @Query("SELECT DISTINCT p.lesson.id FROM UserPerformance p WHERE p.user.id = :userId")
    List<Long> findCompletedLessonIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(p.wpm) FROM UserPerformance p WHERE p.user.id = :userId")
    Double findAverageWpmByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT p.lesson.id) FROM UserPerformance p WHERE p.user.id = :userId")
    long countDistinctLessonsByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);

    // Last 10 sessions for AI profiling
    @Query("SELECT p FROM UserPerformance p JOIN FETCH p.lesson WHERE p.user.id = :userId ORDER BY p.completedAt DESC")
    List<UserPerformance> findRecentByUserIdWithLesson(@Param("userId") Long userId);

    // All performances with user, used for admin aggregate view (avoids N+1)
    @Query("SELECT p FROM UserPerformance p JOIN FETCH p.user ORDER BY p.wpm DESC")
    List<UserPerformance> findAllWithUser();

    @Query("SELECT COUNT(DISTINCT p2.user.id) FROM UserPerformance p2 " +
           "WHERE p2.user.id != :userId " +
           "AND (SELECT COALESCE(MAX(p3.wpm), 0) FROM UserPerformance p3 WHERE p3.user.id = p2.user.id) > " +
           "    (SELECT COALESCE(MAX(p4.wpm), 0) FROM UserPerformance p4 WHERE p4.user.id = :userId)")
    long countUsersRankedHigher(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM UserPerformance p")
    long countTypists();

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPerformance p WHERE p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPerformance p WHERE p.user.id = :userId AND p.lesson.id IN :lessonIds")
    void deleteByUserIdAndLessonIdIn(@Param("userId") Long userId, @Param("lessonIds") java.util.List<Long> lessonIds);
}
