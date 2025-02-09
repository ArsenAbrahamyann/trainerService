package org.example.trainer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.example.trainer.dto.request.TrainerWorkloadRequest;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.repository.TrainerWorkloadRepository;
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

    private TrainerWorkloadRequest addRequest;
    private TrainerWorkloadRequest deleteRequest;

    private TrainerWorkloadEntity trainerWorkloadEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        addRequest = new TrainerWorkloadRequest("trainer1",
                "John", "Doe", true,
                LocalDate.of(2025, 2, 1), 10, "ADD");

        deleteRequest = new TrainerWorkloadRequest("trainer1",
                "John", "Doe", true,
                LocalDate.of(2025, 2, 1), 5, "DELETE");

        trainerWorkloadEntity = new TrainerWorkloadEntity();
        trainerWorkloadEntity.setTrainerUsername("trainer1");
        trainerWorkloadEntity.setTotalTrainingDuration(10);
    }

    @Test
    void testUpdateTrainingHours_DeleteAction() {
        // Arrange
        when(workloadRepository.findByTrainerUsername(any())).thenReturn(Optional.of(trainerWorkloadEntity));

        // Act
        trainerWorkloadService.updateTrainingHours(deleteRequest);

        // Assert
        assertThat(trainerWorkloadEntity.getTotalTrainingDuration()).isEqualTo(5);
        verify(workloadRepository, times(1)).save(trainerWorkloadEntity);
    }

    @Test
    void testUpdateTrainingHours_DeleteActionWithNoWorkload() {
        // Arrange
        when(workloadRepository.findByTrainerUsername(any())).thenReturn(Optional.empty());

        // Act
        trainerWorkloadService.updateTrainingHours(deleteRequest);

        // Assert:
        verify(workloadRepository, never()).save(any());
    }

    @Test
    void testUpdateTrainingHours_NewTrainer() {
        // Arrange
        when(workloadRepository.findByTrainerUsername(any())).thenReturn(Optional.empty());

        // Act
        trainerWorkloadService.updateTrainingHours(addRequest);

        // Assert:
        verify(workloadRepository, times(1)).save(any(TrainerWorkloadEntity.class));
    }

}
