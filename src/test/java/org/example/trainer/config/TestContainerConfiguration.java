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
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

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

    @Bean
    public MongoDBContainer mongoDbcontainer() {
        return mongoDBContainer;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(System.getProperty("spring.activemq.broker-url"));
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Bean
    public TrainerWorkloadRepository trainerWorkloadRepository() {
        return Mockito.mock(TrainerWorkloadRepository.class);
    }

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
}
