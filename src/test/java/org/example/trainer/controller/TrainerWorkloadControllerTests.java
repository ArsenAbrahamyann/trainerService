package org.example.trainer.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.example.trainer.dto.response.TrainerWorkloadResponse;
import org.example.trainer.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TrainerWorkloadControllerTests {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private TrainerWorkloadController trainerWorkloadController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(trainerWorkloadController).build();
    }


    @Test
    public void testGetTrainerWorkload_Success() throws Exception {
        // Arrange
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setTrainerUsername("trainer1");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setActive(true);
        response.setWorkload(Map.of(2025, Map.of(2, 10)));

        when(trainerWorkloadService.getTrainingHours("trainer1")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/trainer-workload/trainer1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("trainer1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.workload['2025']['2']").value(10));

        verify(trainerWorkloadService, times(1)).getTrainingHours("trainer1");
    }


    @Test
    public void testGetTrainerWorkload_NotFound() throws Exception {
        // Arrange
        when(trainerWorkloadService.getTrainingHours("trainer1")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/trainer-workload/trainer1"))
                .andExpect(status().isNotFound());

        verify(trainerWorkloadService, times(1)).getTrainingHours("trainer1");
    }
}
