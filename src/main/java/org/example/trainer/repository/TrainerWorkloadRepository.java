package org.example.trainer.repository;

import java.util.Optional;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkloadEntity, Long> {
    Optional<TrainerWorkloadEntity> findByTrainerUsername(String trainerUsername);
}
