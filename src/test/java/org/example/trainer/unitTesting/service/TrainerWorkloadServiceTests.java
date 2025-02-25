package org.example.trainer.unitTesting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.trainer.dto.request.TrainerWorkloadRequestDto;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.example.trainer.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TrainerWorkloadServiceTests {
    @Mock
    private TrainerWorkloadRepository workloadRepository;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateTrainingHours_DeleteAction_NoExistingWorkload() {
        // Arrange
        TrainerWorkloadRequestDto requestDto = new TrainerWorkloadRequestDto("trainer1", "John",
                "Doe", true,
                LocalDate.of(2025, 2, 1), 5, "DELETE");
        when(workloadRepository.findByTrainerUsername("trainer1")).thenReturn(Optional.empty());

        // Act
        trainerWorkloadService.updateTrainingHours(requestDto);

        // Assert
        verify(workloadRepository, never()).save(any(TrainerWorkloadEntity.class));
        verify(workloadRepository).findByTrainerUsername("trainer1");
    }

    @Test
    void testUpdateTrainingHours_AddAction_NewTrainer() {
        // Arrange
        TrainerWorkloadRequestDto requestDto = new TrainerWorkloadRequestDto("trainer2",
                "Jane", "Doe", true,
                LocalDate.of(2025, 3, 15), 10, "ADD");
        when(workloadRepository.findByTrainerUsername("trainer2")).thenReturn(Optional.empty());

        // Act
        trainerWorkloadService.updateTrainingHours(requestDto);

        // Assert
        verify(workloadRepository).save(any(TrainerWorkloadEntity.class));
    }

    @Test
    void testUpdateTrainingHours_AddTrainingHours_ExistingTrainer() {
        // Arrange
        Map<Integer, Integer> monthlyData = new HashMap<>();
        monthlyData.put(3, 10);  // March

        Map<Integer, Map<Integer, Integer>> yearlyData = new HashMap<>();
        yearlyData.put(2025, monthlyData);

        TrainerWorkloadEntity existingEntity = new TrainerWorkloadEntity("1", "trainer1",
                "John", "Doe", true, yearlyData);

        TrainerWorkloadRequestDto requestDto = new TrainerWorkloadRequestDto("trainer1",
                "John", "Doe", true,
                LocalDate.of(2025, 3, 1), 5, "ADD");
        when(workloadRepository.findByTrainerUsername("trainer1")).thenReturn(Optional.of(existingEntity));

        // Act
        trainerWorkloadService.updateTrainingHours(requestDto);

        // Assert
        verify(workloadRepository).save(any(TrainerWorkloadEntity.class));
        assertThat(existingEntity.getTrainingSummary().get(2025).get(3)).isEqualTo(15);
    }

    @Test
    void testUpdateTrainingHours_DeleteTrainingHours_ExistingTrainer() {
        // Arrange
        Map<Integer, Integer> monthlyData = new HashMap<>();
        monthlyData.put(2, 10);  // February

        Map<Integer, Map<Integer, Integer>> yearlyData = new HashMap<>();
        yearlyData.put(2025, monthlyData);

        TrainerWorkloadEntity existingEntity = new TrainerWorkloadEntity("1", "trainer1",
                "John", "Doe", true, yearlyData);

        TrainerWorkloadRequestDto requestDto = new TrainerWorkloadRequestDto("trainer1",
                "John", "Doe", true,
                LocalDate.of(2025, 2, 1), 5, "DELETE");
        when(workloadRepository.findByTrainerUsername("trainer1")).thenReturn(Optional.of(existingEntity));

        // Act
        trainerWorkloadService.updateTrainingHours(requestDto);

        // Assert
        verify(workloadRepository).save(any(TrainerWorkloadEntity.class));
        assertThat(existingEntity.getTrainingSummary().get(2025).get(2)).isEqualTo(5);
    }
}
