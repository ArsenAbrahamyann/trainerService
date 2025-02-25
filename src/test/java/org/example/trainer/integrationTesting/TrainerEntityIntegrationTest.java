package org.example.trainer.integrationTesting;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import javax.jms.JMSException;
import javax.jms.Message;
import org.example.trainer.dto.request.TrainerWorkloadRequestDto;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.example.trainer.service.JmsConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TrainerEntityIntegrationTest {

    @Autowired
    private JmsConsumerService jmsConsumerService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    private TrainerWorkloadRequestDto request;
    private ObjectMapper objectMapper;



    @BeforeEach
    void setUp() {
        request = new TrainerWorkloadRequestDto(
                "trainer1", "John", "Doe", true, LocalDate.now(),
                5, "ADD");
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testReceiveTrainingUpdate() throws JsonProcessingException {
        String jsonMessage = objectMapper.writeValueAsString(request);
        jmsTemplate.convertAndSend("trainer.training.update", jsonMessage);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        TrainerWorkloadEntity trainer = trainerWorkloadRepository
                .findByTrainerUsername(request.getTrainerUsername()).orElse(null);
        assertThat(trainer).isNotNull();
        assertThat(trainer.getTrainerUsername()).isEqualTo(request.getTrainerUsername());
    }

    @Test
    public void testHandleTrainingHoursRequest() throws JMSException {
        jmsTemplate.send("request.traininghours.queue", session -> {
            Message message = session.createTextMessage();
            message.setStringProperty("trainerUsername", "trainer1");
            message.setIntProperty("month", 1);
            message.setJMSCorrelationID("unique-correlation-id");
            return message;
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Message receivedMessage = jmsTemplate.receiveSelected("response.traininghours.queue",
                "JMSCorrelationID = 'unique-correlation-id'");
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getJMSCorrelationID()).isEqualTo("unique-correlation-id");
    }
}
