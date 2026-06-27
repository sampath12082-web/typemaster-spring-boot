package com.typingtutor.repository;

import com.typingtutor.entity.AuditLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    default List<AuditLog> findLatest(int limit) {
        return findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);
}
