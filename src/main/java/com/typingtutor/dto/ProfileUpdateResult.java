package com.typingtutor.dto;

import com.typingtutor.entity.User;

public record ProfileUpdateResult(User user, String devOtp, boolean emailChanged) {}
