package org.example.trainer.integration;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.example.trainer.config.TestContainerConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"org.example.trainer.integration"},
        features = {"classpath:feature_Integration/trainerServiceIntegration.feature"},
        plugin = {"pretty", "json:target/cucumber-report.json"}
)
@CucumberContextConfiguration
@ContextConfiguration(classes = TestContainerConfiguration.class)
public class CucumberTestRunner {
}



