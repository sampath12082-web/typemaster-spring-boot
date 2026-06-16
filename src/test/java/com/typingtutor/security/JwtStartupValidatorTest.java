package com.typingtutor.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JwtStartupValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtStartupValidator.class);

    @Test
    void whenJwtSecretIsValid_thenContextStarts() {
        contextRunner.withPropertyValues(
                // base64 for "this-is-a-thirty-two-byte-secret" (32 bytes)
                "app.jwt.secret=dGhpcy1pcy1hLXRoaXJ0eS10d28tYnl0ZS1zZWNyZXQ=",
                "app.jwt.expiration-ms=3600000"
        ).run(context -> {
            assertThatCode(() -> context.getBean(JwtStartupValidator.class)).doesNotThrowAnyException();
            assertThat(context).hasSingleBean(JwtStartupValidator.class);
        });
    }

    @Test
    void whenJwtSecretIsInvalidBase64_thenContextFails() {
        contextRunner.withPropertyValues(
                "app.jwt.secret=not-base64",
                "app.jwt.expiration-ms=3600000"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(findValidatorFailure(context))
                    .hasMessageContaining("Failed to decode app.jwt.secret");
        });
    }

    @Test
    void whenJwtSecretTooShort_thenContextFails() {
        contextRunner.withPropertyValues(
                "app.jwt.secret=Zm9vYmFy", // base64 for "foobar"
                "app.jwt.expiration-ms=3600000"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(findValidatorFailure(context))
                    .hasMessageContaining("decodes to fewer than 32 bytes");
        });
    }

    /**
     * Spring wraps the {@code @PostConstruct} failure in BeanCreationException, and our own
     * IllegalStateException may itself wrap a decoding cause — so neither the top-level failure
     * nor the deepest root cause is reliably the exception JwtStartupValidator actually threw.
     * Walk the chain for it directly instead of assuming a fixed depth.
     */
    private static IllegalStateException findValidatorFailure(AssertableApplicationContext context) {
        Throwable cause = context.getStartupFailure();
        while (cause != null && !(cause instanceof IllegalStateException)) {
            cause = cause.getCause();
        }
        assertThat(cause).isInstanceOf(IllegalStateException.class);
        return (IllegalStateException) cause;
    }
}
