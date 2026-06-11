package com.typingtutor.repository;

import com.typingtutor.entity.EmailVerification;
import com.typingtutor.entity.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    @Query("SELECT v FROM EmailVerification v WHERE v.user.id = :userId AND v.purpose = :purpose AND v.used = false ORDER BY v.createdAt DESC")
    Optional<EmailVerification> findLatestUnused(@Param("userId") Long userId, @Param("purpose") VerificationPurpose purpose);

    @Query("SELECT v FROM EmailVerification v JOIN FETCH v.user WHERE v.user.email = :email AND v.purpose = :purpose AND v.used = false ORDER BY v.createdAt DESC")
    Optional<EmailVerification> findLatestUnusedByEmail(@Param("email") String email, @Param("purpose") VerificationPurpose purpose);
}
