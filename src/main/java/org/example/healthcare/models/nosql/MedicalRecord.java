package org.example.healthcare.models.nosql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {
    @Id
    private String id;

    @Indexed
    private Long patientId;

    private String patientName;

    private LocalDate recordDate;

    private String title;

    @Field("description")
    private String description;

    @Field("prescription_id")
    private String prescriptionId;  // Links to Prescription (which has doctor info)

    @CreatedDate
    private LocalDateTime createdAt;

}