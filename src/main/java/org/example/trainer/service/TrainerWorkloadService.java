package org.example.trainer.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.dto.request.TrainerWorkloadRequestDto;
import org.example.trainer.dto.response.TrainerWorkloadResponseDto;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.exeption.WorkloadException;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

/**
 * Service class for managing training hours of trainers.
 * This includes updating training hours based on specified actions and retrieving
 * accumulated training hours for a specific month.
 */
@Service
@Slf4j
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository workloadRepository;

    /**
     * Constructs a TrainerWorkloadService with the necessary dependency.
     *
     * @param workloadRepository Repository for accessing trainer workload data
     */
    public TrainerWorkloadService(TrainerWorkloadRepository workloadRepository) {
        this.workloadRepository = workloadRepository;
    }

    /**
     * Updates the training hours for a trainer based on the provided request data.
     * This can entail adding or deleting training hours for the specified trainer.
     *
     * @param request Contains the details of the trainer and the update parameters like duration and action type
     * @throws IllegalArgumentException if the action type in the request is neither "ADD" nor "DELETE"
     */
    public void updateTrainingHours(TrainerWorkloadRequestDto request) {
        log.info("Updating training hours for trainer: {}", request.getTrainerUsername());

        Optional<TrainerWorkloadEntity> optionalWorkload = workloadRepository
                .findByTrainerUsername(request.getTrainerUsername());

        if (!optionalWorkload.isPresent() && request.getActionType().equalsIgnoreCase("DELETE")) {
            log.warn("Attempt to delete non-existing workload for trainer: {}", request.getTrainerUsername());
            return;
        }

        TrainerWorkloadEntity workload = optionalWorkload.orElseGet(() -> {
            log.info("Creating new workload record for trainer: {}", request.getTrainerUsername());
            TrainerWorkloadEntity newWorkload = new TrainerWorkloadEntity();
            newWorkload.setTrainerUsername(request.getTrainerUsername());
            newWorkload.setFirstName(request.getFirstName());
            newWorkload.setLastName(request.getLastName());
            newWorkload.setActive(request.isActive());
            newWorkload.setTrainingSummary(new HashMap<>());
            return newWorkload;
        });

        Integer year = request.getTrainingDate().getYear();
        Integer month = request.getTrainingDate().getMonthValue();

        workload.getTrainingSummary().computeIfAbsent(year, y -> new HashMap<>())
                .merge(month, request.getTrainingDuration(), (existing, newDuration) -> {
                    if (request.getActionType().equalsIgnoreCase("ADD")) {
                        return existing + newDuration;
                    } else if (request.getActionType().equalsIgnoreCase("DELETE")) {
                        return Math.max(existing - newDuration, 0);
                    }
                    return existing;
                });

        workloadRepository.save(workload);
        log.info("Workload updated successfully for trainer: {}", request.getTrainerUsername());
    }

    /**
     * Retrieves the total training hours for a trainer for a specified month.
     *
     * @param trainerUsername The username of the trainer
     * @param month The specific month for querying total hours
     * @return TrainerWorkloadResponseDto containing detailed training summary for the month
     * @throws WorkloadException if no workload data is found for the trainer or the specific month
     */
    public TrainerWorkloadResponseDto getTrainingHoursForMonth(String trainerUsername, Integer month) {
        log.info("Retrieving training hours for trainer: {} for month: {}", trainerUsername, month);

        TrainerWorkloadEntity workload = workloadRepository.findByTrainerUsername(trainerUsername)
                .orElseThrow(() -> new WorkloadException("No workload data found for trainer: " + trainerUsername));

        Map<Integer, Map<Integer, Integer>> yearData = new HashMap<>();

        workload.getTrainingSummary().forEach((year, months) -> {
            months.getOrDefault(month, 0);
            if (months.containsKey(month)) {
                yearData.computeIfAbsent(year, y -> new HashMap<>()).put(month, months.get(month));
            }
        });

        if (yearData.isEmpty()) {
            throw new WorkloadException("No workload data found for trainer: " + trainerUsername
                    + " for month: " + month);
        }

        return new TrainerWorkloadResponseDto(
                workload.getTrainerUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.isActive(),
                yearData
        );
    }
}
