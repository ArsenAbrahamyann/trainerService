package org.example.trainer.entity;


import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "trainer_workloads")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TrainerWorkloadEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String trainerUsername;

    @Indexed
    private String firstName;

    @Indexed
    private String lastName;

    private boolean isActive;

    private Map<Integer, Map<Integer, Integer>> trainingSummary = new HashMap<>();

}
