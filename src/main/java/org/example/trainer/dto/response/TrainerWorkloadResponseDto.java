package org.example.trainer.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadResponseDto {

    private String trainerUsername;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private Map<Integer, Map<Integer, Integer>> workload;
}
