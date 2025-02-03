package org.example.trainer.config;

import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.logger.TransactionInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuration class for setting up Swagger and other application-level settings.
 * This class includes Swagger OpenAPI configuration, transaction ID logging, and secure random generator.
 * It also registers a {@link TransactionInterceptor} to track transaction IDs across requests.
 * The {@link OpenAPI} bean is configured with the base server URL and basic API information,
 * and it also defines an interceptor for generating and managing unique transaction IDs for each request.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class Config implements WebMvcConfigurer {

    private final TransactionInterceptor transactionInterceptor;

    /**
     * Configures the custom OpenAPI settings for the Gym CRM API documentation.
     * This method sets the base server URL to {@code http://localhost:8080} and includes basic API
     * information such as the title of the API ("Gym CRM").
     *
     * @return a configured {@link OpenAPI} instance with server URL and API info
     */
    @Bean
    public OpenAPI myOpenApi() {
        log.debug("Configuring Custom OpenApi");

        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8081")
                ))
                .info(
                        new Info().title("Trainer Workload Service")
                );

    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Provides a {@link SecureRandom} instance for generating cryptographically strong random numbers.
     * This can be used in various parts of the application requiring secure random number generation.
     *
     * @return a {@link SecureRandom} instance
     */
    @Bean
    public SecureRandom getSecureRandom() {
        return new SecureRandom();
    }

    /**
     * Registers the {@link TransactionInterceptor} to capture and log transaction IDs for every request.
     * The interceptor is applied to all incoming HTTP requests to ensure a unique transaction ID is associated
     * with each request and is logged for traceability.
     *
     * @param registry the {@link InterceptorRegistry} to register the interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionInterceptor)
                .addPathPatterns("/**");
    }


}
