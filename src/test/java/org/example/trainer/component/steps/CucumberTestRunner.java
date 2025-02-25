package org.example.trainer.component.steps;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.example.trainer.config.TestContainerConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = TestContainerConfiguration.class)
@AutoConfigureMockMvc
@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"org.example.trainer.component.steps"},
        features = {"classpath:features/workload.feature"},
        plugin = {"pretty"}
)
public class CucumberTestRunner {
}



