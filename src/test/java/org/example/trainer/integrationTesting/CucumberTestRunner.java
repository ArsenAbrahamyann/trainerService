package org.example.trainer.integrationTesting;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.example.trainer.config.TestContainerConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"org.example.trainer.integrationTesting"},
        features = {"classpath:feature_Integration/trainerServiceIntegration.feature"},
        plugin = {"pretty", "json:target/cucumber-report.json"}
)
@CucumberContextConfiguration
@ContextConfiguration(classes = TestContainerConfiguration.class)
public class CucumberTestRunner {
}



