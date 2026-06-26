package com.typingtutor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typingtutor.entity.DifficultyLevel;
import com.typingtutor.entity.Lesson;
import com.typingtutor.entity.UserPerformance;
import com.typingtutor.repository.LessonRepository;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@Service
public class LessonGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LessonGenerationService.class);
    private static final int MIN_CHARS = 600;
    private static final int MAX_CHARS = 900;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    @Value("${app.ai.api-url:https://api.anthropic.com/v1/messages}")
    private String aiApiUrl;

    @Value("${app.ai.model:claude-haiku-4-5-20251001}")
    private String aiModel;

    @Value("${app.lesson.min-wpm-advanced:50}")
    private int advancedMinWpm;

    @Value("${app.lesson.min-accuracy:85.0}")
    private double minAccuracy;

    private final LessonRepository lessonRepository;
    private final UserPerformanceRepository performanceRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public LessonGenerationService(LessonRepository lessonRepository,
                                   UserPerformanceRepository performanceRepository,
                                   UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.performanceRepository = performanceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Lesson generateAdvancedLessonForUser(Long userId) {
        long standardCount = lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED);
        List<Lesson> advancedLessons = lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED);

        // Verify all standard ADVANCED lessons are passed
        List<UserPerformance> perfs = performanceRepository.findRecentByUserIdWithLesson(userId);
        long passedCount = advancedLessons.stream()
                .filter(l -> !l.isAiGenerated())
                .filter(l -> perfs.stream().anyMatch(p ->
                        p.getLesson().getId().equals(l.getId())
                                && p.getWpm() >= l.getMinWpm()
                                && p.getAccuracyPercentage() >= l.getMinAccuracy()))
                .count();

        if (passedCount < standardCount) {
            throw new IllegalArgumentException(
                "Complete all " + standardCount + " standard ADVANCED lessons before generating new ones. "
                + "(" + passedCount + "/" + standardCount + " passed)");
        }

        String username = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found")).getUsername();

        // Build practice profile
        OptionalDouble avgWpm = perfs.stream().limit(10).mapToInt(UserPerformance::getWpm).average();
        OptionalDouble avgAccuracy = perfs.stream().limit(10)
                .mapToDouble(UserPerformance::getAccuracyPercentage).average();

        // Find weakest lesson by accuracy
        String weakArea = perfs.stream()
                .min((a, b) -> Double.compare(a.getAccuracyPercentage(), b.getAccuracyPercentage()))
                .map(p -> p.getLesson().getTitle())
                .orElse("general typing");

        String generatedContent = callAiApi(
                (int) avgWpm.orElse(60),
                avgAccuracy.orElse(90),
                weakArea);

        int maxOrder = lessonRepository.findMaxDisplayOrderByDifficultyLevel(DifficultyLevel.ADVANCED)
                .orElse(24);

        Lesson lesson = new Lesson();
        lesson.setTitle("AI-Generated: " + weakArea + " Focus");
        lesson.setDifficultyLevel(DifficultyLevel.ADVANCED);
        lesson.setContentText(generatedContent);
        lesson.setDisplayOrder(maxOrder + 1);
        lesson.setMinWpm(advancedMinWpm);
        lesson.setMinAccuracy(minAccuracy);
        lesson.setActive(true);
        lesson.setAiGenerated(true);
        lesson.setGeneratedForUserId(userId);

        Lesson saved = lessonRepository.save(lesson);
        log.info("AI lesson generated for userId={} lessonId={}", userId, saved.getId());
        return saved;
    }

    private String callAiApi(int avgWpm, double avgAccuracy, String weakArea) {
        if (aiApiKey.isBlank()) {
            log.warn("AI_API_KEY not set — returning placeholder lesson content");
            return generatePlaceholderContent(weakArea);
        }
        try {
            String prompt = buildPrompt(avgWpm, avgAccuracy, weakArea);
            String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
                "model", aiModel,
                "max_tokens", 1024,
                "messages", List.of(java.util.Map.of("role", "user", "content", prompt))
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
                log.error("AI API returned status {}: {}", response.statusCode(), response.body());
                return generatePlaceholderContent(weakArea);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("content").get(0).path("text").asText();
            JsonNode jsonNode = objectMapper.readTree(text);
            String content = jsonNode.path("content").asText();

            if (content.length() < MIN_CHARS || content.length() > MAX_CHARS) {
                log.warn("AI content length {} out of bounds [{},{}] — using placeholder", content.length(), MIN_CHARS, MAX_CHARS);
                return generatePlaceholderContent(weakArea);
            }
            return content;
        } catch (Exception e) {
            log.error("AI generation failed: {}", e.getMessage());
            return generatePlaceholderContent(weakArea);
        }
    }

    private String buildPrompt(int avgWpm, double avgAccuracy, String weakArea) {
        return "You are a professional typing tutor content writer. "
                + "The student has completed all standard advanced lessons. "
                + "Their recent stats: average WPM=" + avgWpm + ", average accuracy=" + String.format("%.1f", avgAccuracy) + "%. "
                + "Their weakest area: " + weakArea + ". "
                + "Generate a new advanced typing lesson with these requirements: "
                + "Minimum 600 characters, maximum 900 characters. "
                + "Title: a descriptive 3-5 word title. "
                + "Content: realistic prose (technical writing, literature excerpt, or business writing). "
                + "Vocabulary should include words targeting their weak areas. "
                + "No newlines within the content. "
                + "Respond ONLY with valid JSON: {\"title\": \"...\", \"content\": \"...\"}";
    }

    private String generatePlaceholderContent(String weakArea) {
        return "Mastering advanced typing requires consistent practice and deliberate focus on accuracy before speed. "
                + "Experienced typists understand that muscle memory develops gradually through repetition of correct finger placement. "
                + "The home row keys serve as the foundation for all typing technique, anchoring your fingers in the optimal position. "
                + "Professional typists maintain steady rhythm while adapting fluidly to punctuation, numbers, and special characters throughout. "
                + "Regular practice sessions focusing on specific weak areas accelerate improvement more effectively than general typing exercises alone. "
                + "Building speed naturally follows once accuracy has been thoroughly established as a reliable habit in your daily practice.";
    }
}
