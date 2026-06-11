package com.typingtutor.service;

import com.typingtutor.dto.PlacementResultDto;
import com.typingtutor.entity.DifficultyLevel;
import com.typingtutor.entity.Lesson;
import com.typingtutor.entity.User;
import com.typingtutor.repository.LessonRepository;
import com.typingtutor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {

    @Mock UserRepository userRepository;
    @Mock LessonRepository lessonRepository;

    @InjectMocks PlacementService placementService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("newuser");

        // Inject @Value fields manually since Spring context is not loaded
        ReflectionTestUtils.setField(placementService, "placementPassage",
            "The quick brown fox jumps over the lazy dog.");
        ReflectionTestUtils.setField(placementService, "timeLimitSeconds", 60);
    }

    @Test
    void wpmBelow20RecommendsBasic() {
        Lesson basicFirst = makeLesson(1L, DifficultyLevel.BASIC, 1);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(user));
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.BASIC))
                .thenReturn(List.of(basicFirst));

        PlacementResultDto result = placementService.submitPlacement("newuser", 15, 80.0);

        assertThat(result.getRecommendedTier()).isEqualTo("BASIC");
        assertThat(result.getStartLessonId()).isEqualTo(1L);
        verify(userRepository).save(argThat(u -> u.isPlacementCompleted()
                && "BASIC".equals(u.getRecommendedTier())
                && u.getPlacementWpm() == 15));
    }

    @Test
    void wpm20to39RecommendsIntermediate() {
        Lesson intFirst = makeLesson(9L, DifficultyLevel.INTERMEDIATE, 9);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(user));
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.INTERMEDIATE))
                .thenReturn(List.of(intFirst));

        PlacementResultDto result = placementService.submitPlacement("newuser", 30, 88.0);

        assertThat(result.getRecommendedTier()).isEqualTo("INTERMEDIATE");
        assertThat(result.getStartLessonId()).isEqualTo(9L);
    }

    @Test
    void wpm40plusRecommendsAdvanced() {
        Lesson advFirst = makeLesson(17L, DifficultyLevel.ADVANCED, 17);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(user));
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(advFirst));

        PlacementResultDto result = placementService.submitPlacement("newuser", 55, 92.0);

        assertThat(result.getRecommendedTier()).isEqualTo("ADVANCED");
    }

    @Test
    void getPlacementTestReturnsPassageAndTimeLimit() {
        var test = placementService.getPlacementTest();
        assertThat(test).containsKey("passage");
        assertThat(test).containsKey("timeLimitSeconds");
        assertThat(test.get("passage").toString()).isNotBlank();
        assertThat((int) test.get("timeLimitSeconds")).isEqualTo(60);
    }

    private Lesson makeLesson(Long id, DifficultyLevel level, int order) {
        Lesson l = new Lesson();
        l.setId(id);
        l.setDifficultyLevel(level);
        l.setDisplayOrder(order);
        l.setContentText("test content");
        return l;
    }
}
