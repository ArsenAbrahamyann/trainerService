package org.example.trainer.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.trainer.dto.response.TrainerWorkloadResponse;
import org.example.trainer.service.TrainerWorkloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling trainer workload-related requests.
 */
@RestController
@Slf4j
@RequestMapping("/trainer-workload")
public class TrainerWorkloadController {

    private final TrainerWorkloadService service;

    /**
     * Constructs the controller with the necessary service.
     *
     * @param service Service for handling trainer workload operations.
     */
    public TrainerWorkloadController(TrainerWorkloadService service) {
        this.service = service;
    }

    /**
     * Retrieves the training hours for the specified trainer.
     *
     * @param trainerUsername the identifier for the trainer.
     * @return Map containing training hours data.
     */
    @GetMapping("/{trainerUsername}")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(@PathVariable String trainerUsername) {
        log.info("Retrieving workload summary for trainer: {}", trainerUsername);
        TrainerWorkloadResponse response = service.getTrainingHours(trainerUsername);

        if (response == null) {
            log.info("No workload summary found for trainer: {}", trainerUsername);
            return ResponseEntity.notFound().build();
        }

        log.info("Successfully retrieved workload summary for trainer: {}", trainerUsername);
        return ResponseEntity.ok(response);
    }
}
