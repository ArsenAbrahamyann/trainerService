package org.example.trainer.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.dto.request.TrainerWorkloadRequest;
import org.example.trainer.dto.response.TrainerWorkloadResponse;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.exeption.WorkloadException;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing training hours of trainers.
 */
@Service
@Slf4j
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository workloadRepository;

    /**
     * Initializes a new instance of the TrainerWorkloadService.
     *
     * @param workloadRepository Repository used for accessing and persisting workload data.
     */
    public TrainerWorkloadService(TrainerWorkloadRepository workloadRepository) {
        this.workloadRepository = workloadRepository;
    }

    /**
     * Updates the training hours for a trainer according to the request.
     * An ADD action increases hours whereas a DELETE action decreases hours.
     *
     * @param request Contains the details of the trainer and the training session.
     */
    public void updateTrainingHours(TrainerWorkloadRequest request) {
        log.info("Updating training hours for trainer: {}", request.getTrainerUsername());
        LocalDate trainingDate = request.getTrainingDate();
        Integer month = trainingDate.getMonthValue();
        Integer year = trainingDate.getYear();

        Optional<TrainerWorkloadEntity> existingWorkload = workloadRepository.findByTrainerUsername(
                request.getTrainerUsername());
        if (existingWorkload.isEmpty() && request.getActionType().equalsIgnoreCase("DELETE")) {
            log.info("No workload found to delete for trainer: {}", request.getTrainerUsername());
            return;
        }

        TrainerWorkloadEntity workload = existingWorkload.orElseGet(() -> {
            log.info("Creating new workload record for trainer: {}", request.getTrainerUsername());
            TrainerWorkloadEntity newWorkload = new TrainerWorkloadEntity();
            newWorkload.setTrainerUsername(request.getTrainerUsername());
            newWorkload.setFirstName(request.getFirstName());
            newWorkload.setLastName(request.getLastName());
            newWorkload.setActive(request.isActive());
            newWorkload.setTrainingYear(year);
            newWorkload.setTrainingMonth(month);
            newWorkload.setTotalTrainingDuration(0);
            return newWorkload;
        });

        if (request.getActionType().equalsIgnoreCase("ADD")) {
            workload.setTotalTrainingDuration(workload.getTotalTrainingDuration()
                    + request.getTrainingDuration());
            log.info("Added duration; new total: {} for trainer: {}", workload.getTotalTrainingDuration(),
                    request.getTrainerUsername());
        } else if (request.getActionType().equalsIgnoreCase("DELETE")) {
            int updatedDuration = Math.max(workload.getTotalTrainingDuration()
                    - request.getTrainingDuration(), 0);
            workload.setTotalTrainingDuration(updatedDuration);
            log.info("Deleted duration; new total: {} for trainer: {}", workload.getTotalTrainingDuration(),
                    request.getTrainerUsername());
        }

        workloadRepository.save(workload);
        log.info("Workload updated successfully for trainer: {}", request.getTrainerUsername());
    }

    /**
     * Retrieves the total training hours for a specific trainer by username.
     *
     * @param trainerUsername Username of the trainer.
     * @return Map containing year-month as key and total training duration as value.
     */
    public TrainerWorkloadResponse getTrainingHours(String trainerUsername) {
        log.info("Retrieving training hours for trainer: {}", trainerUsername);

        List<TrainerWorkloadEntity> workloadList = workloadRepository.findAllByTrainerUsername(trainerUsername);

        if (workloadList.isEmpty()) {
            throw new WorkloadException("No workload data found for trainer: "
                    + trainerUsername);
        }

        Map<Integer, Map<Integer, Integer>> workloadSummary = new HashMap<>();

        for (TrainerWorkloadEntity workload : workloadList) {
            workloadSummary
                    .computeIfAbsent(workload.getTrainingYear(), y -> new HashMap<>())
                    .put(workload.getTrainingMonth(), workload.getTotalTrainingDuration());
        }

        TrainerWorkloadEntity firstRecord = workloadList.get(0);

        return new TrainerWorkloadResponse(
                firstRecord.getTrainerUsername(),
                firstRecord.getFirstName(),
                firstRecord.getLastName(),
                firstRecord.isActive(),
                workloadSummary
        );

    }
}
