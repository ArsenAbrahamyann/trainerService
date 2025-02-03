package org.example.trainer.controller;

import java.time.LocalDate;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.trainer.dto.request.TrainerWorkloadRequest;
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
    public void testUpdateWorkload_Success() throws Exception {
        // Arrange
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("trainer1");
        request.setTrainingDate(LocalDate.now());
        request.setActionType("ADD");
        request.setTrainingDuration(10);

        // Since updateTrainingHours is void, use doNothing() for mocking.
        doNothing().when(trainerWorkloadService).updateTrainingHours(any(TrainerWorkloadRequest.class));

        // Act & Assert
        mockMvc.perform(post("/trainer-workload/update")
                        .header("Authorization", "Bearer validToken")
                        .contentType("application/json")
                        .content("{\"trainerUsername\":\"trainer1\",\"trainingDate\":\"2025-02-03\",\"actionType\":\"ADD\",\"trainingDuration\":10}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Workload update successfully"));

        verify(trainerWorkloadService, times(1)).updateTrainingHours(any(TrainerWorkloadRequest.class));
    }

    @Test
    public void testUpdateWorkload_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/trainer-workload/update")
                        .header("Authorization", "InvalidToken")
                        .contentType("application/json")
                        .content("{\"trainerUsername\":\"trainer1\",\"trainingDate\":\"2025-02-03\",\"actionType\":\"ADD\",\"trainingDuration\":10}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTrainerWorkload_Success() throws Exception {
        // Arrange
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setTrainerUsername("trainer1");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setActive(true);
        response.setWorkload(Map.of(2025, Map.of(2, 10))); // Mocked response

        when(trainerWorkloadService.getTrainingHours("trainer1")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/trainer-workload/trainer1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("trainer1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                // Corrected jsonPath to access nested map
                .andExpect(jsonPath("$.workload['2025']['2']").value(10)); // Corrected to access the nested map

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
