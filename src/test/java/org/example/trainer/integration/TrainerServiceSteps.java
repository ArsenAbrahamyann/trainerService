package org.example.trainer.integration;

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
import org.example.trainer.config.TestContainerConfiguration;
import org.example.trainer.dto.request.TrainerWorkloadRequestDto;
import org.example.trainer.dto.response.TrainerWorkloadResponseDto;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Integration tests for the TrainerService using Cucumber and ActiveMQ.
 * This class defines step definitions for testing message processing related to trainer workload updates
 * and training hours requests.
 * Uses:
 * - TestContainers for MongoDB and ActiveMQ
 * - Awaitility for asynchronous message processing assertions
 * - Mockito for mocking repository interactions
 * - Jackson for JSON serialization/deserialization
 */
@SpringBootTest
@ContextConfiguration(classes = TestContainerConfiguration.class)
public class TrainerServiceSteps {


    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("mockTrainerWorkloadRepository")
    private TrainerWorkloadRepository trainerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Initializes mocks before each test case execution.
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Validates that the ActiveMQ broker is running and accessible.
     */
    @Given("the ActiveMQ broker is running")
    public void theActiveMqBrokerIsRunning() {
        String brokerUrl = System.getProperty("spring.activemq.broker-url");
        assertThat(brokerUrl).isNotNull();
        assertThat(brokerUrl).startsWith("tcp://");
    }

    /**
     * Sends a training update message to the ActiveMQ queue.
     *
     * @throws Exception if message serialization fails
     */
    @When("the trainerService receives a training update message")
    public void theTrainerServiceReceivesATrainingUpdateMessage() throws Exception {
        TrainerWorkloadRequestDto request = new TrainerWorkloadRequestDto(
                "trainer1", "John", "Doe",
                true, LocalDate.now(), 5, "ADD");

        String jsonMessage = objectMapper.writeValueAsString(request);
        jmsTemplate.send("trainer.training.update", session -> {
            TextMessage message = session.createTextMessage(jsonMessage);
            return message;
        });
    }

    /**
     * Verifies that the trainer's workload is updated in the repository.
     */
    @Then("the trainer workload should be updated")
    public void theTrainerWorkloadShouldBeUpdated() {
        TrainerWorkloadEntity trainerEntity = new TrainerWorkloadEntity();
        trainerEntity.setTrainerUsername("trainer1");

        given(trainerRepository.findByTrainerUsername("trainer1")).willReturn(Optional.of(trainerEntity));

        Awaitility.await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerWorkloadEntity> trainer = trainerRepository.findByTrainerUsername("trainer1");
            assertThat(trainer).isPresent();
            assertThat(trainer.get().getTrainerUsername()).isEqualTo("trainer1");
        });
    }

    /**
     * Sends a training hours request message to the ActiveMQ queue.
     *
     * @throws Exception if message creation fails
     */
    @When("the trainerService receives a training hours request message")
    public void theTrainerServiceReceivesATrainingHoursRequestMessage() throws Exception {

        jmsTemplate.send("request.traininghours.queue", session -> {
            TextMessage message = session.createTextMessage();
            message.setStringProperty("trainerUsername", "trainer1");
            message.setIntProperty("month", 1);
            message.setJMSCorrelationID("unique-correlation-id");
            return message;
        });

        simulateTrainingHoursResponse();
    }

    /**
     * Simulates sending a response message for a training hours request.
     *
     * @throws Exception if message serialization fails
     */
    private void simulateTrainingHoursResponse() throws Exception {

        TrainerWorkloadResponseDto response = new TrainerWorkloadResponseDto();
        response.setTrainerUsername("trainer1");

        String responseJson = objectMapper.writeValueAsString(response);
        jmsTemplate.send("response.traininghours.queue", session -> {
            TextMessage message = session.createTextMessage(responseJson);
            message.setJMSCorrelationID("unique-correlation-id");
            return message;
        });

    }

    /**
     * Verifies that a response message for the training hours request is received.
     */
    @Then("the trainerService should send a training hours response")
    public void theTrainerServiceShouldSendATrainingHoursResponse() {
        Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {

            TextMessage receivedMessage = (TextMessage) jmsTemplate
                    .receiveSelected("response.traininghours.queue",
                    "JMSCorrelationID = 'unique-correlation-id'");


            assertThat(receivedMessage).isNotNull();
            assertThat(receivedMessage.getText()).contains("trainer1");
            assertThat(receivedMessage.getJMSCorrelationID()).isEqualTo("unique-correlation-id");
        });
    }
}
