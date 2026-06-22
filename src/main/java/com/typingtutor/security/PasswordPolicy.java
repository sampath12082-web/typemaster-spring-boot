package com.typingtutor.security;

import java.util.regex.Pattern;

/**
 * Password complexity rule shared by every flow that sets a password. Previously enforced via
 * {@code @Pattern} directly on the DTO field, but that field now carries RSA-encrypted ciphertext
 * over the wire, so the rule is checked here against the decrypted plaintext instead.
 */
public final class PasswordPolicy {

    public static final String MESSAGE =
            "Password must be 16-20 characters with uppercase, lowercase, digit and special character (@$!%*?&)";

    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{16,20}$");

    private PasswordPolicy() {}

    public static boolean isValid(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }

    public static void validate(String password) {
        if (!isValid(password)) {
            throw new IllegalArgumentException(MESSAGE);
        }
    }
}
