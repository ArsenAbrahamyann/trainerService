package org.example.trainer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.dto.request.TrainerWorkloadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class that acts as a consumer of JMS messages related to trainer workload updates.
 * It receives messages from a specified queue, processes them, and performs operations
 * based on the contents of these messages.
 */
@Service
@Slf4j
public class JmsConsumerService {
    private final TrainerWorkloadService trainerWorkloadService;
    private final ObjectMapper objectMapper;
    private final JmsTemplate jmsTemplate;

    /**
     * Constructs a JmsConsumerService with the necessary dependencies for handling messages.
     *
     * @param trainerWorkloadService the service for updating trainer workload data
     * @param objectMapper the mapper for converting between JSON strings and Java objects
     * @param jmsTemplate the JMS template for sending messages to queues, like a dead letter queue
     */
    @Autowired
    public JmsConsumerService(TrainerWorkloadService trainerWorkloadService,
                              ObjectMapper objectMapper, JmsTemplate jmsTemplate) {
        this.trainerWorkloadService = trainerWorkloadService;
        this.objectMapper = objectMapper;
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Listens to a JMS queue for messages regarding trainer training updates.
     * Processes incoming messages by deserializing them and passing them to
     * the workload service, or by handling errors and sending messages to the DLQ.
     *
     * @param message the JSON string message received from the queue
     */
    @JmsListener(destination = "trainer.training.update", containerFactory = "jmsListenerContainerFactory")
    public void receiveTrainingUpdate(String message) {
        log.info("Received raw message: {}", message);

        if (message == null || message.trim().isEmpty()) {
            log.error("Received an empty or null message! Ignoring...");
            return;
        }

        try {
            log.info("Attempting to deserialize message: {}", message);
            TrainerWorkloadRequest request = objectMapper.readValue(message, TrainerWorkloadRequest.class);
            log.info("Deserialized TrainerWorkloadRequest: {}", request);

            if (request.getTrainerUsername() == null || request.getActionType() == null) {
                throw new IllegalArgumentException("Missing required fields in TrainerWorkloadRequest");
            }

            trainerWorkloadService.updateTrainingHours(request);
            log.info("Successfully processed training update for trainer: {}", request.getTrainerUsername());
        } catch (Exception e) {
            log.error("Failed to process training update message: {}, redirecting to DLQ", message, e);
            sendToDeadLetterQueue(message, e.getMessage());
        }
    }

    /**
     * Sends a formatted message to the dead letter queue (DLQ) when message processing fails.
     * This method includes timestamp and error details in the DLQ message.
     *
     * @param originalMessage the original message that failed processing
     * @param errorMessage the error message describing why the processing failed
     */
    private void sendToDeadLetterQueue(String originalMessage, String errorMessage) {
        try {
            String dlqMessage = objectMapper.writeValueAsString(Map.of(
                    "originalMessage", originalMessage,
                    "error", errorMessage,
                    "timestamp", System.currentTimeMillis()
            ));
            jmsTemplate.convertAndSend("trainer.training.dlq", dlqMessage);
            log.info("Message sent to Dead Letter Queue: {}", dlqMessage);
        } catch (Exception e) {
            log.error("Failed to send message to Dead Letter Queue", e);
        }
    }
}
