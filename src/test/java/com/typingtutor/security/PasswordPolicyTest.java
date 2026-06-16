package com.typingtutor.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void validPassword_isValid() {
        assertThat(PasswordPolicy.isValid("New@Password1234")).isTrue();
    }

    @Test
    void missingUppercase_isInvalid() {
        assertThat(PasswordPolicy.isValid("nouppercase@1234")).isFalse();
    }

    @Test
    void missingLowercase_isInvalid() {
        assertThat(PasswordPolicy.isValid("NOLOWERCASE@1234")).isFalse();
    }

    @Test
    void missingDigit_isInvalid() {
        assertThat(PasswordPolicy.isValid("NoDigitsPass@")).isFalse();
    }

    @Test
    void missingSpecialChar_isInvalid() {
        assertThat(PasswordPolicy.isValid("NoSpecial1234A")).isFalse();
    }

    @Test
    void tooShort_isInvalid() {
        assertThat(PasswordPolicy.isValid("Sh@1")).isFalse();
    }

    @Test
    void exactlyEightChars_isValid() {
        assertThat(PasswordPolicy.isValid("Abcd@123")).isTrue();
    }

    @Test
    void nullPassword_isInvalid() {
        assertThat(PasswordPolicy.isValid(null)).isFalse();
    }

    @Test
    void validate_throwsOnInvalidPassword() {
        assertThatThrownBy(() -> PasswordPolicy.validate("weak"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PasswordPolicy.MESSAGE);
    }

    @Test
    void validate_doesNotThrowOnValidPassword() {
        PasswordPolicy.validate("New@Password1234");
    }
}
