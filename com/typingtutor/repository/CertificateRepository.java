package com.typingtutor.repository;

import com.typingtutor.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId);
    Optional<Certificate> findByCertificateId(String certificateId);
}
