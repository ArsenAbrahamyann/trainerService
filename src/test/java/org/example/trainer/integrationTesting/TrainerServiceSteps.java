package org.example.trainer.integrationTesting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.jms.TextMessage;
import org.awaitility.Awaitility;
import org.example.trainer.dto.request.TrainerWorkloadRequestDto;
import org.example.trainer.dto.response.TrainerWorkloadResponseDto;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.config.TestContainerConfiguration;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestContainerConfiguration.class)
public class TrainerServiceSteps {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceSteps.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository trainerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Given("the ActiveMQ broker is running")
    public void theActiveMqBrokerIsRunning() {
        String brokerUrl = System.getProperty("spring.activemq.broker-url");
        log.info("ActiveMQ broker is running at {}", brokerUrl);
        assertThat(brokerUrl).isNotNull();
        assertThat(brokerUrl).startsWith("tcp://");
    }

    @When("the trainerService receives a training update message")
    public void theTrainerServiceReceivesATrainingUpdateMessage() throws Exception {
        TrainerWorkloadRequestDto request = new TrainerWorkloadRequestDto(
                "trainer1", "John", "Doe",
                true, LocalDate.now(), 5, "ADD");

        String jsonMessage = objectMapper.writeValueAsString(request);
        log.info("Sending message to trainer.training.update queue: {}", jsonMessage);
        jmsTemplate.send("trainer.training.update", session -> {
            TextMessage message = session.createTextMessage(jsonMessage);
            return message;
        });
    }

    @Then("the trainer workload should be updated")
    public void theTrainerWorkloadShouldBeUpdated() {
        TrainerWorkloadEntity trainerEntity = new TrainerWorkloadEntity();
        trainerEntity.setTrainerUsername("trainer1");

        given(trainerRepository.findByTrainerUsername("trainer1")).willReturn(Optional.of(trainerEntity));

        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            log.info("Checking if the trainer workload is updated...");
            Optional<TrainerWorkloadEntity> trainer = trainerRepository.findByTrainerUsername("trainer1");
            assertThat(trainer).isPresent();
            assertThat(trainer.get().getTrainerUsername()).isEqualTo("trainer1");
        });
    }

    @When("the trainerService receives a training hours request message")
    public void theTrainerServiceReceivesATrainingHoursRequestMessage() throws Exception {
        log.info("Sending training hours request message...");

        jmsTemplate.send("request.traininghours.queue", session -> {
            TextMessage message = session.createTextMessage();
            message.setStringProperty("trainerUsername", "trainer1");
            message.setIntProperty("month", 1);
            message.setJMSCorrelationID("unique-correlation-id");
            log.info("Message sent with JMSCorrelationID: unique-correlation-id");
            return message;
        });

        simulateTrainingHoursResponse();
    }

    private void simulateTrainingHoursResponse() throws Exception {
        log.info("Simulating training hours response message...");

        TrainerWorkloadResponseDto response = new TrainerWorkloadResponseDto();
        response.setTrainerUsername("trainer1");

        String responseJson = objectMapper.writeValueAsString(response);
        jmsTemplate.send("response.traininghours.queue", session -> {
            TextMessage message = session.createTextMessage(responseJson);
            message.setJMSCorrelationID("unique-correlation-id");
            return message;
        });

        log.info("Sent simulated response message with JMSCorrelationID: unique-correlation-id");
    }

    @Then("the trainerService should send a training hours response")
    public void theTrainerServiceShouldSendATrainingHoursResponse() {
        Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            log.info("Waiting for training hours response message...");

            TextMessage receivedMessage = (TextMessage) jmsTemplate
                    .receiveSelected("response.traininghours.queue",
                    "JMSCorrelationID = 'unique-correlation-id'");

            log.info("Message received: {}", receivedMessage != null ? receivedMessage.getText() : "No message");

            assertThat(receivedMessage).isNotNull();
            assertThat(receivedMessage.getText()).contains("trainer1");
            assertThat(receivedMessage.getJMSCorrelationID()).isEqualTo("unique-correlation-id");
        });
    }
}
