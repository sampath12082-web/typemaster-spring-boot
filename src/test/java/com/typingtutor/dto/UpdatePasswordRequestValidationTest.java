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
    void newPassword_missingUppercase_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword("nouppercase@1234");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void newPassword_missingLowercase_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword("NOLOWERCASE@1234");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void newPassword_missingDigit_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword("NoDigitsPass@");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void newPassword_missingSpecialChar_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword("NoSpecial1234A");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void newPassword_tooShort_failsValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword("Sh@1");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void newPassword_exactlyEightChars_passesValidation() {
        UpdatePasswordRequest req = validRequest();
        req.setNewPassword("Abcd@123");
        assertThat(validator.validate(req)).isEmpty();
    }

    private UpdatePasswordRequest validRequest() {
        UpdatePasswordRequest req = new UpdatePasswordRequest();
        req.setCurrentPassword("Old@Password1234");
        req.setNewPassword("New@Password1234");
        return req;
    }
}
