package org.example.trainer.service;

import org.example.trainer.dto.request.TrainerWorkloadRequest;
import org.example.trainer.dto.response.TrainerWorkloadResponse;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.exeption.WorkloadException;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.*;

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

        // Initialize a request for ADD action
        addRequest = new TrainerWorkloadRequest("trainer1", "John", "Doe", true,
                LocalDate.of(2025, 2, 1), 10, "ADD");

        // Initialize a request for DELETE action
        deleteRequest = new TrainerWorkloadRequest("trainer1", "John", "Doe", true,
                LocalDate.of(2025, 2, 1), 5, "DELETE");

        // Initialize a trainer workload entity
        trainerWorkloadEntity = new TrainerWorkloadEntity();
        trainerWorkloadEntity.setTrainerUsername("trainer1");
        trainerWorkloadEntity.setTotalTrainingDuration(10);
    }

    @Test
    void testGetTrainingHours_ExistingTrainer() {
        // Arrange
        when(workloadRepository.findByTrainerUsername(any())).thenReturn(Optional.of(trainerWorkloadEntity));

        // Act
        TrainerWorkloadResponse response = trainerWorkloadService.getTrainingHours("trainer1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTrainerUsername()).isEqualTo("trainer1");

        // Access the nested map: year -> (month -> total training duration)
        Map<Integer, Map<Integer, Integer>> workloadSummary = response.getWorkload();

        // Assert that the workload map contains the expected year
        assertThat(workloadSummary).containsKey(trainerWorkloadEntity.getTrainingYear());

        // Get the nested map for the year
        Map<Integer, Integer> monthToDuration = workloadSummary.get(trainerWorkloadEntity.getTrainingYear());

        // Assert that the map for the year contains the correct month and total training duration
        assertThat(monthToDuration).containsEntry(trainerWorkloadEntity.getTrainingMonth(), trainerWorkloadEntity.getTotalTrainingDuration());
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

        // Assert: No action should be performed
        verify(workloadRepository, never()).save(any());
    }

    @Test
    void testUpdateTrainingHours_NewTrainer() {
        // Arrange
        when(workloadRepository.findByTrainerUsername(any())).thenReturn(Optional.empty());

        // Act
        trainerWorkloadService.updateTrainingHours(addRequest);

        // Assert: A new workload record is created
        verify(workloadRepository, times(1)).save(any(TrainerWorkloadEntity.class));
    }

}
