package org.example.trainer.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import org.example.trainer.dto.request.TrainerWorkloadRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

@ExtendWith(MockitoExtension.class)
public class JmsConsumerServiceTest {

    @Mock private TrainerWorkloadService trainerWorkloadService;
    @Mock private ObjectMapper objectMapper;
    @Mock private JmsTemplate jmsTemplate;
    @Mock private MapMessage mapMessage;

    @InjectMocks private JmsConsumerService jmsConsumerService;

    @Test
    void receiveTrainingUpdate_withValidMessage_processesUpdate() throws Exception {
        // Arrange
        final String validMessage = "{\"trainerUsername\":\"john.doe\",\"actionType\":\"ADD\"}";
        TrainerWorkloadRequestDto workloadRequest = new TrainerWorkloadRequestDto("john.doe",
                "john", "doe", true, LocalDate.now(), 50, "ADD");
        when(objectMapper.readValue(validMessage, TrainerWorkloadRequestDto.class)).thenReturn(workloadRequest);

        // Act
        jmsConsumerService.receiveTrainingUpdate(validMessage);

        // Assert
        verify(trainerWorkloadService).updateTrainingHours(workloadRequest);
    }

    @Test
    void receiveTrainingUpdate_withEmptyMessage_ignoresProcessing() {
        // Act
        jmsConsumerService.receiveTrainingUpdate("");

        // Assert
        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void receiveTrainingUpdate_withInvalidJson_logsError() throws Exception {
        // Arrange
        final String invalidJsonMessage = "{\"invalidJson++}";
        when(objectMapper.readValue(invalidJsonMessage, TrainerWorkloadRequestDto.class))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // Act
        jmsConsumerService.receiveTrainingUpdate(invalidJsonMessage);

        // Assert
        verify(jmsTemplate).convertAndSend(eq("trainer.training.update.dlq"), contains("Invalid JSON"));
    }



    @Test
    void handleTrainingHoursRequest_withJmsException_logsError() throws Exception {
        // Arrange
        doThrow(new JMSException("JMS failure", " errorCode")).when(mapMessage).getString(anyString());

        // Act
        jmsConsumerService.handleTrainingHoursRequest(mapMessage);

        // Assert
        verify(jmsTemplate).convertAndSend(eq("trainer.training.update.dlq"), contains("JMS failure"));
    }
}
