package org.example.healthcare.models.nosql;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Field("prescription_ids")
    @Builder.Default
    private List<String> prescriptionIds = new ArrayList<>();

    @Field("lab_reports")
    @Builder.Default
    private List<String> labReports = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}