package org.example.trainer.config;

import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * Configuration for setting up Java Message Service (JMS) with ActiveMQ as the message broker.
 * This class provides beans necessary for creating JMS connections and allows for message
 * sending and asynchronous message receiving through listeners.
 */
@Configuration
public class JmsConfig {

    @Value("${spring.activemq.broker-url}")
    String brokerUrl;
    @Value("${spring.activemq.user}")
    String brokerUsername;
    @Value("${spring.activemq.password}")
    String brokerPassword;


    /**
     * Creates and configures a ConnectionFactory for use with the ActiveMQ broker.
     * The ConnectionFactory is configured with broker URL, username, and password,
     * which are injected via application properties.
     *
     * @return a configured {@link ConnectionFactory} for ActiveMQ.
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setPassword(brokerUsername);
        connectionFactory.setUserName(brokerPassword);
        return connectionFactory;
    }

    /**
     * Configures a {@link JmsTemplate} that is used for sending messages.
     * The JmsTemplate is configured with a {@link ConnectionFactory} to establish
     * connections to the ActiveMQ broker.
     *
     * @param connectionFactory the injected {@link ConnectionFactory} bean.
     * @return a configured {@link JmsTemplate} for sending JMS messages.
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    /**
     * Configures a {@link DefaultJmsListenerContainerFactory} which sets up listener containers
     * for message-driven POJOs with the MessageListener interface. The factory supports
     * concurrent listeners.
     *
     * @param connectionFactory the injected {@link ConnectionFactory} bean used to establish
     *                          connections to the ActiveMQ broker.
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
