package com.typingtutor.service;

import com.typingtutor.entity.AuditLog;
import com.typingtutor.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock AuditLogRepository auditLogRepository;

    @InjectMocks AuditLogService auditLogService;

    // ── log() ──────────────────────────────────────────────────────────

    @Test
    void logSavesEntityWithCorrectFields() {
        auditLogService.log("admin", "DELETE_USER", "Deleted user id=42");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getAction()).isEqualTo("DELETE_USER");
        assertThat(saved.getDetails()).isEqualTo("Deleted user id=42");
    }

    @Test
    void logWithNullDetailsSavesSuccessfully() {
        auditLogService.log("admin", "LOGIN", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertThat(captor.getValue().getDetails()).isNull();
    }

    @Test
    void logSilentlyCatchesRepositoryException() {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("DB connection lost"));

        // Should NOT propagate
        auditLogService.log("admin", "RESET_PASSWORD", "user=student");

        verify(auditLogRepository).save(any(AuditLog.class));
        // No exception thrown — the catch block absorbed it
    }

    // ── getMyActivity() ────────────────────────────────────────────────

    @Test
    void getMyActivityReturnsListFromRepository() {
        AuditLog entry1 = new AuditLog("admin", "LOGIN", null);
        AuditLog entry2 = new AuditLog("admin", "DELETE_USER", "id=5");
        when(auditLogRepository.findByUsernameOrderByCreatedAtDesc("admin"))
                .thenReturn(List.of(entry1, entry2));

        List<AuditLog> result = auditLogService.getMyActivity("admin");

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(entry1, entry2);
        verify(auditLogRepository).findByUsernameOrderByCreatedAtDesc("admin");
    }

    @Test
    void getMyActivityReturnsEmptyListWhenNoEntries() {
        when(auditLogRepository.findByUsernameOrderByCreatedAtDesc("ghost"))
                .thenReturn(List.of());

        List<AuditLog> result = auditLogService.getMyActivity("ghost");

        assertThat(result).isEmpty();
    }

    // ── getLatest() ────────────────────────────────────────────────────

    @Test
    void getLatestDelegatesToRepository() {
        AuditLog entry = new AuditLog("admin", "CREATE_USER", "user=newbie");
        when(auditLogRepository.findLatest(5)).thenReturn(List.of(entry));

        List<AuditLog> result = auditLogService.getLatest(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo("CREATE_USER");
        verify(auditLogRepository).findLatest(5);
    }

    @Test
    void getLatestReturnsEmptyListWhenNoLogs() {
        when(auditLogRepository.findLatest(10)).thenReturn(List.of());

        List<AuditLog> result = auditLogService.getLatest(10);

        assertThat(result).isEmpty();
    }
}
