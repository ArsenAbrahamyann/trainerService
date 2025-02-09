package org.example.trainer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.example.trainer.dto.request.TrainerWorkloadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

@ExtendWith(MockitoExtension.class)
public class JmsConsumerServiceTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private JmsConsumerService jmsConsumerService;

    private final String validMessage = "{\"trainerUsername\":\"johndoe\",\"actionType\":\"increase\"}";
    private final TrainerWorkloadRequest workloadRequest = new TrainerWorkloadRequest();

    @BeforeEach
    void setUp() throws Exception {
        workloadRequest.setTrainerUsername("johndoe");
        workloadRequest.setActionType("increase");
        when(objectMapper.readValue(validMessage, TrainerWorkloadRequest.class)).thenReturn(workloadRequest);
    }

    @Test
    void testReceiveTrainingUpdate_Successful() throws Exception {
        jmsConsumerService.receiveTrainingUpdate(validMessage);

        verify(trainerWorkloadService).updateTrainingHours(workloadRequest);
        verify(jmsTemplate, never()).convertAndSend(eq("trainer.training.dlq"), anyString());
    }

    @Test
    void testReceiveTrainingUpdate_ProcessingFailure() throws Exception {
        doThrow(new RuntimeException("Processing failed")).when(trainerWorkloadService)
                .updateTrainingHours(workloadRequest);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("formatted_error_message");

        jmsConsumerService.receiveTrainingUpdate(validMessage);

        verify(jmsTemplate).convertAndSend("trainer.training.dlq", "formatted_error_message");
    }

}
