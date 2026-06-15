package com.typingtutor.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtStartupValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtStartupValidator.class);

    @Test
    void whenJwtSecretIsValid_thenContextStarts() {
        contextRunner.withPropertyValues(
                "app.jwt.secret=c29tZXZlcnlzdHJvbmdzZWNyZXQxMjM0NTY=",
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
            assertThatThrownBy(() -> context.getBean(JwtStartupValidator.class))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed to decode app.jwt.secret");
        });
    }

    @Test
    void whenJwtSecretTooShort_thenContextFails() {
        contextRunner.withPropertyValues(
                "app.jwt.secret=Zm9vYmFy", // base64 for "foobar"
                "app.jwt.expiration-ms=3600000"
        ).run(context -> {
            assertThatThrownBy(() -> context.getBean(JwtStartupValidator.class))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("decodes to fewer than 32 bytes");
        });
    }
}
