package com.typingtutor.service;

import com.typingtutor.dto.InquiryDto;
import com.typingtutor.entity.Inquiry;
import com.typingtutor.entity.User;
import com.typingtutor.repository.InquiryRepository;
import com.typingtutor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public InquiryService(InquiryRepository inquiryRepository, UserRepository userRepository) {
        this.inquiryRepository = inquiryRepository;
        this.userRepository = userRepository;
    }

    public InquiryDto submitInquiry(String username, String subject, String message) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        Inquiry inq = new Inquiry();
        inq.setUser(user);
        inq.setSubject(subject);
        inq.setMessage(message);
        return InquiryDto.from(inquiryRepository.save(inq));
    }

    public List<InquiryDto> getMyInquiries(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().map(InquiryDto::from).collect(Collectors.toList());
    }
}
