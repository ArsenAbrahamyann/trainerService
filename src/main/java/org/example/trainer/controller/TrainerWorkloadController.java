package org.example.trainer.controller;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.example.trainer.dto.request.TrainerWorkloadRequest;
import org.example.trainer.dto.response.TrainerWorkloadResponse;
import org.example.trainer.service.TrainerWorkloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

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
     * @param service Service for handling trainer workload operations.
     */
    public TrainerWorkloadController(TrainerWorkloadService service) {
        this.service = service;
    }

    /**
     * Updates trainer workload based on provided update details.
     * @param token Authorization token.
     * @param request contains trainer and training session details.
     * @return Indicates the outcome of updating the workload.
     */
    @PostMapping("/update")
    public ResponseEntity<String> updateWorkload(@RequestHeader("Authorization") String token,
                                                 @RequestBody @Valid TrainerWorkloadRequest request) {
        log.info("Received request to update workload for trainer: {}", request.getTrainerUsername());
        if (!(token != null && token.startsWith("Bearer "))) {
            log.error("Unauthorized access attempt with token: {}", token);
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
        }
        service.updateTrainingHours(request);
        log.info("Workload updated successfully for trainer: {}", request.getTrainerUsername());
        return ResponseEntity.ok("Workload update successfully");
    }

    /**
     * Retrieves the training hours for the specified trainer.
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
