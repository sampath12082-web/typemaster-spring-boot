package com.typingtutor.repository;

import com.typingtutor.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId);
    Optional<Certificate> findByCertificateId(String certificateId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Certificate c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
