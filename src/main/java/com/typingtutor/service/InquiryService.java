package com.typingtutor.service;

import com.typingtutor.dto.InquiryDto;
import com.typingtutor.entity.Inquiry;
import com.typingtutor.entity.InquiryStatus;
import com.typingtutor.entity.User;
import com.typingtutor.repository.InquiryRepository;
import com.typingtutor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class InquiryService {

    private static final Logger log = LoggerFactory.getLogger(InquiryService.class);
    private static final int MAX_REOPENS = 3;

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public InquiryService(InquiryRepository inquiryRepository, UserRepository userRepository,
                          AuditLogService auditLogService) {
        this.inquiryRepository = inquiryRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public InquiryDto submitInquiry(String username, String subject, String message) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        Inquiry inq = new Inquiry();
        inq.setUser(user);
        inq.setSubject(subject);
        inq.setMessage(message);
        InquiryDto result = InquiryDto.from(inquiryRepository.save(inq));
        auditLogService.log(username, "INQUIRY_SUBMITTED", "subject=" + subject);
        return result;
    }

    public List<InquiryDto> getMyInquiries(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().map(InquiryDto::from).collect(Collectors.toList());
    }

    @Transactional
    public InquiryDto reopenInquiry(Long inquiryId, String username, String reason) {
        Inquiry inq = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new NoSuchElementException("Inquiry not found: " + inquiryId));

        if (!inq.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only reopen your own inquiries.");
        }
        if (inq.getStatus() != InquiryStatus.RESOLVED) {
            throw new IllegalArgumentException("Only resolved inquiries can be reopened.");
        }
        if (inq.getReopenCount() >= MAX_REOPENS) {
            throw new IllegalArgumentException("Maximum reopen limit (" + MAX_REOPENS + ") reached.");
        }

        inq.setStatus(InquiryStatus.REOPENED);
        inq.setReopenCount(inq.getReopenCount() + 1);
        inq.setReopenReason(reason);
        inq.setLastReopenedAt(LocalDateTime.now());
        log.debug("Inquiry {} reopened by {} (reopen #{})", inquiryId, username, inq.getReopenCount());
        InquiryDto result = InquiryDto.from(inquiryRepository.save(inq));
        auditLogService.log(username, "INQUIRY_REOPENED",
                "inquiryId=" + inquiryId + " reopenCount=" + inq.getReopenCount());
        return result;
    }
}
