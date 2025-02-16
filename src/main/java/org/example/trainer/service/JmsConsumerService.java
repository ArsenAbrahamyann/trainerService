package org.example.trainer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.dto.request.TrainerWorkloadRequestDto;
import org.example.trainer.dto.response.TrainerWorkloadResponseDto;
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
     * @param objectMapper           the mapper for converting between JSON strings and Java objects
     * @param jmsTemplate            the JMS template for sending messages to queues, like a dead letter queue
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

        if (message
                == null
                || message.trim().isEmpty()) {
            log.error("Received an empty or null message! Ignoring...");
            return;
        }

        try {
            log.info("Attempting to deserialize message: {}", message);
            TrainerWorkloadRequestDto request = objectMapper.readValue(message, TrainerWorkloadRequestDto.class);
            log.info("Deserialized TrainerWorkloadRequest: {}", request);

            if (request.getTrainerUsername() == null || request.getActionType() == null) {
                throw new IllegalArgumentException("Missing required fields in TrainerWorkloadRequest");
            }

            trainerWorkloadService.updateTrainingHours(request);
            log.info("Successfully processed training update for trainer: {}", request.getTrainerUsername());
        } catch (Exception e) {
            log.error("Failed to process training update message: {}, redirecting to DLQ", message, e);
            sendToDeadLetterQueue(message, e);
        }
    }

    /**
     * Listener to handle incoming JMS requests for training hours.
     * Receives a message from the request queue, processes it, and sends back a response.
     *
     * @param message the JMS message received from the "request.traininghours.queue"
     */
    @JmsListener(destination = "request.traininghours.queue")
    public void handleTrainingHoursRequest(javax.jms.Message message) {
        try {
            if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                String trainerUsername = mapMessage.getString("trainerUsername");
                Integer month = mapMessage.getInt("month");
                String correlationId = mapMessage.getString("correlationId");

                log.info("Received training hours request for {} (month: {}) with correlationId = {}", trainerUsername,
                        month, correlationId);

                TrainerWorkloadResponseDto responseDto = trainerWorkloadService.getTrainingHoursForMonth(
                        trainerUsername, month);

                sendTrainingHoursResponse(responseDto, correlationId);

            } else {
                log.error("Received invalid message type: {}", message.getClass().getName());
            }
        } catch (JMSException e) {
            log.error("Error processing request message.", e);
            sendToDeadLetterQueue(String.valueOf(message), e);
        }
    }

    /**
     * Sends the response with the training hours to the response queue.
     *
     * @param responseDto   The response DTO containing the calculated workload.
     * @param correlationId The correlation ID for the response.
     */
    private void sendTrainingHoursResponse(TrainerWorkloadResponseDto responseDto, String correlationId) {
        try {
            String responsePayload = objectMapper.writeValueAsString(responseDto);

            jmsTemplate.convertAndSend("response.traininghours.queue", responsePayload, message -> {
                message.setJMSCorrelationID(correlationId);
                return message;
            });

            log.info("Sent response for correlationId = {}", correlationId);

        } catch (Exception e) {
            log.error("Error sending response message for correlationId = {}", correlationId, e);
            sendToDeadLetterQueue("Error sending response message", e);
        }
    }

    /**
     * Sends the failed message to the Dead Letter Queue (DLQ) for further investigation.
     *
     * @param message   the message that failed to process
     * @param exception the exception encountered while processing the message
     */
    private void sendToDeadLetterQueue(String message, Exception exception) {
        try {
            String errorDetails = "Failed message: "
                    + message
                    + "\nError: "
                    + exception.getMessage();
            jmsTemplate.convertAndSend("trainer.training.update.dlq", errorDetails);
            log.info("Failed message sent to DLQ");
        } catch (Exception e) {
            log.error("Failed to send message to DLQ", e);
        }
    }

}
