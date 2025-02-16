package org.example.trainer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerHoursDto {
    private String trainerUsername;
    private Integer month;
    private String correlationId;
}
