package org.example.trainer.config;

import javax.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * Configuration class for setting up Java Message Service (JMS) with ActiveMQ as the message broker.
 * This class provides necessary beans for creating JMS connections, sending messages,
 * and setting up message-driven listeners for asynchronous message processing.
 */
@Configuration
@Slf4j
public class JmsConfig {

    @Value("${spring.activemq.broker-url}")
    String brokerUrl;
    @Value("${spring.activemq.user}")
    String brokerUsername;
    @Value("${spring.activemq.password}")
    String brokerPassword;


    /**
     * Creates and configures a {@link ConnectionFactory} to establish connections to the ActiveMQ broker.
     * The connection is configured using the broker URL, username, and password, which are injected
     * via application properties.
     *
     * @return a configured {@link ConnectionFactory} for ActiveMQ.
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(brokerUsername);
        connectionFactory.setPassword(brokerPassword);
        return connectionFactory;
    }

    /**
     * Configures a {@link MessageConverter} that is responsible for converting messages to and from
     * JMS message formats. The default converter used here is the {@link MappingJackson2MessageConverter},
     * which supports converting Java objects to JSON and vice versa.
     *
     * @return a configured {@link MessageConverter} for message conversion.
     */
    @Bean
    public MessageConverter messageConverter() {
        return new MappingJackson2MessageConverter();
    }


    /**
     * Configures a {@link JmsTemplate} to send JMS messages to the ActiveMQ broker.
     * The JmsTemplate is set up with a {@link ConnectionFactory} to establish connections to the broker.
     *
     * @param connectionFactory the {@link ConnectionFactory} bean used to create JMS connections.
     * @return a configured {@link JmsTemplate} for sending JMS messages.
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    /**
     * Configures a {@link DefaultJmsListenerContainerFactory} which is used to create listener containers
     * for processing incoming JMS messages asynchronously. The factory is set to support concurrent listeners
     * with a concurrency range of 3 to 10.
     *
     * @param connectionFactory the {@link ConnectionFactory} bean used to establish JMS connections.
     * @return a configured {@link DefaultJmsListenerContainerFactory} for asynchronous message processing.
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("3-10");
        return factory;
    }
}
