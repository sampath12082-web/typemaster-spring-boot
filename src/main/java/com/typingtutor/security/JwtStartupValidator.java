package com.typingtutor.security;

import io.jsonwebtoken.io.Decoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class JwtStartupValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtStartupValidator.class);

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @PostConstruct
    public void validate() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            String msg = "app.jwt.secret is not set. Set a base64-encoded secret in application properties.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            String msg = "Failed to decode app.jwt.secret as base64: " + e.getMessage();
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
        if (keyBytes.length < 32) {
            String msg = "app.jwt.secret decodes to fewer than 32 bytes — use a sufficiently long base64-encoded secret.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        log.info("app.jwt.secret validated: {} bytes", keyBytes.length);
    }
}
