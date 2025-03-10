package org.example.trainer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.trainer.repository.TrainerWorkloadRepository;
import org.junit.AfterClass;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

/**
 * Configuration class for setting up TestContainers for integration testing.
 * This class initializes MongoDB and ActiveMQ containers and provides necessary beans
 * for testing the application.
 */
@Configuration
@EnableJms
public class TestContainerConfiguration {
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");
    static GenericContainer<?> activeMq = new GenericContainer<>("rmohr/activemq:latest")
            .withExposedPorts(61616);

    static {
        mongoDBContainer.start();
        activeMq.start();

        System.setProperty("spring.activemq.broker-url", "tcp://" + activeMq.getHost()
                + ":" + activeMq.getMappedPort(61616));
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        System.setProperty("ACTIVEMQ_BROKER_URL", System.getProperty("spring.activemq.broker-url"));
        System.setProperty("ACTIVEMQ_USERNAME", "admin");
        System.setProperty("ACTIVEMQ_PASSWORD", "admin");
    }

    /**
     * Provides the MongoDB test container bean.
     *
     * @return MongoDBContainer instance.
     */
    @Bean
    public MongoDBContainer mongoDbcontainer() {
        return mongoDBContainer;
    }

    /**
     * Provides the ActiveMQ connection factory bean.
     *
     * @return ActiveMQConnectionFactory instance.
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(System.getProperty("spring.activemq.broker-url"));
    }

    /**
     * Provides the JMS template for message handling.
     *
     * @param connectionFactory The ActiveMQ connection factory.
     * @return JmsTemplate instance.
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }


    /**
     * Configures the ObjectMapper bean for JSON processing.
     *
     * @return Configured ObjectMapper instance.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Provides a mock TrainerWorkloadRepository bean for testing.
     *
     * @return Mocked TrainerWorkloadRepository instance.
     */
    @Bean(name = "mockTrainerWorkloadRepository")
    public TrainerWorkloadRepository trainerWorkloadRepository() {
        return Mockito.mock(TrainerWorkloadRepository.class);
    }

    /**
     * Allows overriding bean definitions in the Spring context.
     *
     * @return BeanDefinitionRegistryPostProcessor instance.
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor allowBeanDefinitionOverriding() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                ((DefaultListableBeanFactory) registry).setAllowBeanDefinitionOverriding(true);
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                // No processing needed
            }
        };
    }

    /**
     * Stops the test containers after all tests are completed.
     */
    @AfterClass
    public static void tearDown() {
        mongoDBContainer.stop();
        activeMq.stop();
    }
}
