package com.typingtutor.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

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
    void dateOfBirthAtLeastEightYearsOldIsValid() {
        RegisterRequest req = validRequest();
        req.setDateOfBirth(LocalDate.now().minusYears(8));

        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void dateOfBirthYoungerThanEightIsRejected() {
        RegisterRequest req = validRequest();
        req.setDateOfBirth(LocalDate.now().minusYears(8).plusDays(1));

        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirthAllowed"));
    }

    @Test
    void futureDateOfBirthIsRejected() {
        RegisterRequest req = validRequest();
        req.setDateOfBirth(LocalDate.now().plusDays(1));

        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirthAllowed"));
    }

    private RegisterRequest validRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("validuser");
        req.setEmail("valid@example.com");
        req.setPassword("Test@1234");
        req.setFullName("Valid User");
        req.setDateOfBirth(LocalDate.now().minusYears(20));
        req.setStudent(false);
        return req;
    }
}
