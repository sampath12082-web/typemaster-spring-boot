package com.typingtutor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HelpAgentService}.
 *
 * <p>This service creates its own {@link java.net.http.HttpClient} internally,
 * so the HTTP-calling path cannot be isolated with {@code @Mock} alone.
 * Tests here cover every branch reachable without an actual HTTP call:
 * blank/null input guards, missing API key fallback, and response shape.
 * Full integration testing of the Anthropic API call would require a mock
 * HTTP server (e.g. WireMock).
 */
@ExtendWith(MockitoExtension.class)
class HelpAgentServiceTest {

    private HelpAgentService helpAgentService;

    @BeforeEach
    void setUp() {
        helpAgentService = new HelpAgentService();
        // Default: no API key configured
        ReflectionTestUtils.setField(helpAgentService, "aiApiKey", "");
        ReflectionTestUtils.setField(helpAgentService, "aiApiUrl", "https://api.anthropic.com/v1/messages");
        ReflectionTestUtils.setField(helpAgentService, "aiModel", "claude-haiku-4-5-20251001");
    }

    // ── Blank / null input guard ───────────────────────────────────────

    @Test
    void chatReturnsPromptWhenMessageIsNull() {
        Map<String, Object> result = helpAgentService.chat(null, null);

        assertThat(result.get("answer")).isEqualTo("Please type your question.");
        assertThat(result.get("escalate")).isEqualTo(false);
    }

    @Test
    void chatReturnsPromptWhenMessageIsEmpty() {
        Map<String, Object> result = helpAgentService.chat("", null);

        assertThat(result.get("answer")).isEqualTo("Please type your question.");
        assertThat(result.get("escalate")).isEqualTo(false);
    }

    @Test
    void chatReturnsPromptWhenMessageIsBlank() {
        Map<String, Object> result = helpAgentService.chat("   ", null);

        assertThat(result.get("answer")).isEqualTo("Please type your question.");
        assertThat(result.get("escalate")).isEqualTo(false);
    }

    // ── Missing API key fallback ───────────────────────────────────────

    @Test
    void chatReturnsFallbackWhenApiKeyIsEmpty() {
        ReflectionTestUtils.setField(helpAgentService, "aiApiKey", "");

        Map<String, Object> result = helpAgentService.chat("How do I reset my password?", null);

        assertThat(result.get("answer")).asString()
                .contains("AI assistant is not available");
        assertThat(result.get("escalate")).isEqualTo(true);
    }

    @Test
    void chatReturnsFallbackWhenApiKeyIsBlank() {
        ReflectionTestUtils.setField(helpAgentService, "aiApiKey", "   ");

        Map<String, Object> result = helpAgentService.chat("What is WPM?", null);

        assertThat(result.get("answer")).asString()
                .contains("AI assistant is not available");
        assertThat(result.get("escalate")).isEqualTo(true);
    }

    // ── Response shape ─────────────────────────────────────────────────

    @Test
    void fallbackResponseContainsRequiredKeys() {
        Map<String, Object> result = helpAgentService.chat("Tell me about certificates", null);

        assertThat(result).containsKeys("answer", "escalate");
    }

    @Test
    void nullInputResponseContainsRequiredKeys() {
        Map<String, Object> result = helpAgentService.chat(null, null);

        assertThat(result).containsKeys("answer", "escalate");
    }

    // ── Construction ───────────────────────────────────────────────────

    @Test
    void serviceCanBeConstructedWithDefaults() {
        // HelpAgentService has no constructor-injected dependencies;
        // verify it instantiates without error.
        HelpAgentService service = new HelpAgentService();
        assertThat(service).isNotNull();
    }
}
