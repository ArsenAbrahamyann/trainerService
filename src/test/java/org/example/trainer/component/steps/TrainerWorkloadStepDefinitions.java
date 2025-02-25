package org.example.trainer.component.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.example.trainer.dto.response.TrainerWorkloadResponseDto;
import org.example.trainer.exeption.WorkloadException;
import org.example.trainer.service.TrainerWorkloadService;
import org.mockito.Mock;

public class TrainerWorkloadStepDefinitions {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    private Exception responseException;

    public TrainerWorkloadStepDefinitions() {
        trainerWorkloadService = mock(TrainerWorkloadService.class);
    }


    @When("I add {int} training hours for {string} on {string}")
    public void i_add_training_hours_for(int hours, String trainerUsername, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int year = date.getYear();
        int month = date.getMonthValue();

        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainingHoursForMonth(trainerUsername, month);

        Map<Integer, Integer> yearlyData = response.getWorkload().computeIfAbsent(year, k -> new HashMap<>());
        yearlyData.merge(month, hours, Integer::sum);

        given(trainerWorkloadService.getTrainingHoursForMonth(trainerUsername, month)).willReturn(response);
    }
    @Then("the total training hours for {string} in month {int} for year {int} should be {int}")
    public void the_total_training_hours_for_in_month_for_year_should_be(String trainerUsername, int month,
                                                                         int year, int expectedHours) {
        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainingHoursForMonth(trainerUsername, month);
        assertNotNull("Response from workload service should not be null", response);

        Map<Integer, Map<Integer, Integer>> workload = response.getWorkload();
        assertTrue("Expected year " + year + " data to be available in workload", workload.containsKey(year));

        Map<Integer, Integer> yearlyData = workload.get(year);
        assertTrue("Expected month " + month + " data to be available for year " + year,
                yearlyData.containsKey(month));

        int actualHours = yearlyData.get(month);
        assertEquals("Expected " + expectedHours + " training hours but found " + actualHours,
                expectedHours, actualHours);
    }



    @Given("a trainer {string} does not exist")
    public void a_trainer_does_not_exist(String trainerUsername) {
        willThrow(new WorkloadException("No workload data"))
                .given(trainerWorkloadService)
                .getTrainingHoursForMonth(eq(trainerUsername), anyInt());

        int currentYear = LocalDate.now().getYear();

        given(trainerWorkloadService.getTrainingHoursForMonth(eq(trainerUsername), anyInt())).willAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String username = (String) args[0];
            int month = (int) args[1];

            TrainerWorkloadResponseDto newResponse = new TrainerWorkloadResponseDto();
            Map<Integer, Map<Integer, Integer>> newWorkload = new HashMap<>();

            Map<Integer, Integer> yearlyData = new HashMap<>();
            yearlyData.put(currentYear, 0);

            newWorkload.put(month, yearlyData);
            newResponse.setTrainerUsername(username);
            newResponse.setWorkload(newWorkload);
            return newResponse;
        });
    }

    @When("I request training hours for {string} in month {int} for year {int}")
    public void i_request_training_hours_for(String trainerUsername, int month, int year) {
        try {
            TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainingHoursForMonth(trainerUsername,
                    month);
        } catch (Exception ex) {
            responseException = ex;
        }
    }


    @Then("{string} exception should be thrown")
    public void exception_should_be_thrown(String message) {
        assertNotNull(responseException);
        assertEquals(message, responseException.getMessage());
    }

    @When("I delete {int} training hours for {string} on {string}")
    public void i_delete_training_hours_for(Integer hours, String trainerUsername, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int month = date.getMonthValue();
        int year = date.getYear();

        TrainerWorkloadResponseDto currentData = trainerWorkloadService.getTrainingHoursForMonth(trainerUsername,
                month);
        Map<Integer, Map<Integer, Integer>> workload = currentData.getWorkload();
        Map<Integer, Integer> monthlyData = workload.getOrDefault(year, new HashMap<>());

        int updatedHours = monthlyData.getOrDefault(month, 0) - hours;
        monthlyData.put(month, updatedHours);
        workload.put(year, monthlyData);

        given(trainerWorkloadService.getTrainingHoursForMonth(trainerUsername, month)).willReturn(currentData);
    }

    @Then("a {string} exception should be thrown")
    public void a_exception_should_be_thrown(String message) {
        assertNotNull("Expected an exception to be thrown", responseException);
        assertEquals("Exception message does not match", message, responseException.getMessage());
    }

    @Then("a record should be created for {string} and the total hours in {string} for {string} should be {int}")
    public void a_record_should_be_created_for_and_the_total_hours_in_for_should_be(String trainerUsername,
                                                                                    String monthStr, String yearStr,
                                                                                    int expectedHours) {
        int month = Integer.parseInt(monthStr);
        int year = Integer.parseInt(yearStr);

        TrainerWorkloadResponseDto response = trainerWorkloadService.getTrainingHoursForMonth(trainerUsername, month);
        assertNotNull("No training data found for the trainer", response);
        assertNotNull("No workload data found for the year", response.getWorkload().get(year));

        int actualHours = response.getWorkload().get(year).getOrDefault(month, 0);
        assertEquals("Training hours do not match", expectedHours, actualHours);
    }

    @Given("there is an existing trainer {string} with {int} training hours in month {int} of year {int}")
    public void there_is_an_existing_trainer_with_training_hours(String trainerUsername, int hours,
                                                                 int month, int year) {
        Map<Integer, Integer> monthlyHours = new HashMap<>();
        monthlyHours.put(month, hours);

        Map<Integer, Map<Integer, Integer>> yearlyHours = new HashMap<>();
        yearlyHours.put(year, monthlyHours);

        TrainerWorkloadResponseDto mockResponse = new TrainerWorkloadResponseDto();
        mockResponse.setTrainerUsername(trainerUsername);
        mockResponse.setWorkload(yearlyHours);

        given(trainerWorkloadService.getTrainingHoursForMonth(trainerUsername, month)).willReturn(mockResponse);
    }


    @Given("there is a trainer {string}")
    public void thereIsATrainer(String trainerName) {
        given(trainerWorkloadService.getTrainingHoursForMonth(eq(trainerName), anyInt()))
                .willThrow(new WorkloadException("No workload data"));
    }

    @Given("there is a new training with trainer {string}")
    public void thereIsANewTrainingWhitTrainer(String trainerName) {
        Map<Integer, Integer> monthlyHours = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> yearlyHours = new HashMap<>();

        yearlyHours.put(2021, monthlyHours);
        monthlyHours.put(9, 0);

        TrainerWorkloadResponseDto mockResponse = new TrainerWorkloadResponseDto();
        mockResponse.setTrainerUsername(trainerName);
        mockResponse.setWorkload(yearlyHours);

        given(trainerWorkloadService.getTrainingHoursForMonth(trainerName, 9)).willReturn(mockResponse);
    }
}
