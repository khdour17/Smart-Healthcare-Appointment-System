package org.example.healthcare.repository.nosql;

import org.example.healthcare.models.nosql.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    List<MedicalRecord> findByPatientId(Long patientId);

    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(Long patientId);
}
