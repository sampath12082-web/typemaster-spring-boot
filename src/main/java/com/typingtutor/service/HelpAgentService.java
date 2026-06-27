package com.typingtutor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class HelpAgentService {

    private static final Logger log = LoggerFactory.getLogger(HelpAgentService.class);

    private static final String SYSTEM_PROMPT = """
            You are the TypeMaster support agent — a friendly, concise, and helpful assistant.
            Answer questions about the TypeMaster typing-tutor application using only the product knowledge below.
            Be warm, encouraging, and clear. Keep answers under 150 words.
            If the user asks something not covered by the product knowledge, set escalate=true and apologise politely.
            Always respond with a JSON object: {"answer": "<your response>", "escalate": <true|false>}.

            === TypeMaster Product Knowledge ===

            WPM: (characters typed ÷ 5) ÷ elapsed minutes. Backspace does not subtract from the count.
            Accuracy: (correct keystrokes ÷ total keystrokes) × 100%. Each character press is counted.
            Grades: S = 95%+ accuracy & 40+ WPM · A = 90%+ · B = 80%+ · C = below 80%.

            Tiers: BASIC, INTERMEDIATE, ADVANCED. Each has 8 lessons. Lessons unlock in order:
            - Intermediate unlocks after passing enough Basic lessons (20 WPM, 85% acc per lesson).
            - Advanced unlocks after passing enough Intermediate lessons (35 WPM, 85% acc per lesson).
            - Advanced requires 50 WPM and 85% accuracy.

            Placement test: 60-second test when first signing in. Determines starting tier. Can be skipped (starts at Basic).
            Exams: Timed exam unlocked after passing all 8 lessons in a tier. Cannot be paused. Results in a certificate.
            Certificates: Earned by passing a tier exam. Downloadable as PDF, shareable via link.
            AI lessons: Unlocked after completing all standard Advanced lessons. Personalised to your weak areas.

            Email verification: 6-digit OTP sent to email, valid 30 minutes. Resend link available on verification page.
            Password reset: Forgot Password → email OTP → set new password. Password must be 16-20 characters with uppercase, lowercase, digit, and special character (@$!%*?&).
            Account settings: Update email, full name, date of birth, student/professional status from Profile page.

            Support tickets: Submit on the Help page (subject + message). Admin responds within 24 hours for escalated issues.
            Backspace: Disabled by design during typing practice — errors stay to build discipline.

            Data saving: Progress saves automatically when a lesson completes. Ensure you are logged in.
            Logout: Available in the top navigation bar on every page.
            """;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    @Value("${app.ai.api-url:https://api.anthropic.com/v1/messages}")
    private String aiApiUrl;

    @Value("${app.ai.model:claude-haiku-4-5-20251001}")
    private String aiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Map<String, Object> chat(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return Map.of("answer", "Please type your question.", "escalate", false);
        }
        if (aiApiKey.isBlank()) {
            log.warn("AI_API_KEY not set — returning fallback help response");
            return Map.of(
                "answer", "Our AI assistant is not available right now. Please submit a support ticket and our team will respond within 24 hours.",
                "escalate", true
            );
        }
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", aiModel,
                "max_tokens", 512,
                "system", SYSTEM_PROMPT,
                "messages", List.of(Map.of("role", "user", "content", userMessage))
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiApiUrl))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", aiApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Help agent API error {}: {}", response.statusCode(), response.body());
                return fallback();
            }

            JsonNode root = objectMapper.readTree(response.body());
            String text   = root.path("content").get(0).path("text").asText();
            JsonNode parsed = objectMapper.readTree(text);
            return Map.of(
                "answer",   parsed.path("answer").asText("Our team will be in touch shortly."),
                "escalate", parsed.path("escalate").asBoolean(false)
            );
        } catch (Exception e) {
            log.error("Help agent failed: {}", e.getMessage());
            return fallback();
        }
    }

    private Map<String, Object> fallback() {
        return Map.of(
            "answer", "I'm having trouble right now. Please submit a support ticket and our team will respond within 24 hours.",
            "escalate", true
        );
    }
}
