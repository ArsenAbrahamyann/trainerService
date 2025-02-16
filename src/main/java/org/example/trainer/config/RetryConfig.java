package org.example.trainer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuration class for setting up retry functionality using Spring Retry.
 * This class provides a {@link RetryTemplate} bean that defines retry and backoff policies
 * for operations that may require retrying due to transient failures.
 */
@Configuration
public class RetryConfig {

    /**
     * Creates a {@link RetryTemplate} bean with custom retry and backoff policies.
     * The retry policy is configured to allow a maximum of 3 retries, and the backoff policy
     * uses an exponential backoff with an initial interval of 1000 milliseconds and a multiplier of 2.0.
     *
     * @return a {@link RetryTemplate} with the configured retry and backoff policies.
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate template = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        template.setBackOffPolicy(backOffPolicy);
        template.setRetryPolicy(retryPolicy);
        return template;
    }
}
