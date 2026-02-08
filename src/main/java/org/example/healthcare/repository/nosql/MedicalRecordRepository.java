package org.example.healthcare.repository.nosql;

import org.example.healthcare.models.nosql.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
}
