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
    void fifteenChars_isInvalid() {
        // "Abcdefg@Hij1234" — 15 chars, one below the 16-char minimum
        assertThat(PasswordPolicy.isValid("Abcdefg@Hij1234")).isFalse();
    }

    @Test
    void exactlySixteenChars_isValid() {
        // "Abcdefg@Hij12345" — exactly 16 chars, the new minimum
        assertThat(PasswordPolicy.isValid("Abcdefg@Hij12345")).isTrue();
    }

    @Test
    void exactlyTwentyChars_isValid() {
        // "Abcdefghij@Klm12345" + 1 — exactly 20 chars, the new maximum
        assertThat(PasswordPolicy.isValid("Abcdefghij@Klmno1234")).isTrue();
    }

    @Test
    void twentyOneChars_isInvalid() {
        // one char over the new 20-char maximum
        assertThat(PasswordPolicy.isValid("Abcdefghij@Klmno12345")).isFalse();
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
