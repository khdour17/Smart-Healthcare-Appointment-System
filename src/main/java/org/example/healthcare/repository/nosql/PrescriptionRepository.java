package org.example.healthcare.repository.nosql;

import org.example.healthcare.models.nosql.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
}
