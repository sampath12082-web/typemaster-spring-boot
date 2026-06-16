package com.typingtutor.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdatePasswordRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void validRequest_passesValidation() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void blankCurrentPassword_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setCurrentPassword("");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
    }

    @Test
    void nullCurrentPassword_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setCurrentPassword(null);
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
    }

    @Test
    void blankNewPassword_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword("");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void nullNewPassword_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword(null);
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    // Password complexity rules (uppercase/lowercase/digit/special/length) no longer apply at the
    // DTO level — this field carries RSA-OAEP ciphertext over the wire. That rule is checked
    // against the decrypted plaintext in AuthController via PasswordPolicy; see PasswordPolicyTest.

    private UpdatePasswordRequest validRequest() {
        UpdatePasswordRequest req = new UpdatePasswordRequest();
        req.setCurrentPassword("Old@Password1234");
        req.setNewPassword("New@Password1234");
        return req;
    }
}
