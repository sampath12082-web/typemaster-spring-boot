package com.typingtutor.service;

import com.typingtutor.dto.AdminUserDto;
import com.typingtutor.dto.InquiryDto;
import com.typingtutor.entity.*;
import com.typingtutor.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";

    private final UserRepository userRepository;
    private final UserPerformanceRepository performanceRepository;
    private final InquiryRepository inquiryRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository,
                        UserPerformanceRepository performanceRepository,
                        InquiryRepository inquiryRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.performanceRepository = performanceRepository;
        this.inquiryRepository = inquiryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .filter(u -> u.getRole() != Role.ADMIN)
            .map(u -> {
                List<UserPerformance> perfs = performanceRepository.findByUserIdOrderByWpmDesc(u.getId());
                AdminUserDto dto = new AdminUserDto();
                dto.setId(u.getId());
                dto.setUsername(u.getUsername());
                dto.setEmail(u.getEmail());
                dto.setRole(u.getRole().name());
                dto.setTotalRuns(perfs.size());
                if (!perfs.isEmpty()) {
                    dto.setAvgWpm(perfs.stream().mapToInt(UserPerformance::getWpm).average().orElse(0));
                    dto.setAvgAccuracy(perfs.stream().mapToDouble(p -> p.getAccuracyPercentage()).average().orElse(0));
                    dto.setBestWpm(perfs.stream().mapToInt(UserPerformance::getWpm).max().orElse(0));
                }
                return dto;
            }).collect(Collectors.toList());
    }

    public AdminUserDto createUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        User user = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(Role.USER)
            .build();
        user = userRepository.save(user);
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    @Transactional
    public void deleteUser(Long userId) {
        performanceRepository.deleteByUserId(userId);
        inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .forEach(inquiryRepository::delete);
        userRepository.deleteById(userId);
    }

    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        return tempPassword;
    }

    public List<InquiryDto> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(InquiryDto::from)
            .collect(Collectors.toList());
    }

    public InquiryDto resolveInquiry(Long inquiryId, String response) {
        Inquiry inq = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new NoSuchElementException("Inquiry not found: " + inquiryId));
        inq.setStatus(InquiryStatus.RESOLVED);
        inq.setAdminResponse(response);
        return InquiryDto.from(inquiryRepository.save(inq));
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
