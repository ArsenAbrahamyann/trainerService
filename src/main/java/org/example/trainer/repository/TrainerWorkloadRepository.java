package org.example.trainer.repository;

import java.util.Optional;
import org.example.trainer.entity.TrainerWorkloadEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadEntity, String> {
    Optional<TrainerWorkloadEntity> findByTrainerUsername(String trainerUsername);
}
