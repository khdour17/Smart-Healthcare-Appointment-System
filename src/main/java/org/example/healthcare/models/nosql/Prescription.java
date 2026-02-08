package org.example.healthcare.models.nosql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Document(collection = "prescriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    @Id
    private String id;

    @Indexed
    private Long appointmentId;

    @Indexed
    private Long patientId;

    @Indexed
    private Long doctorId;

    private String doctorName;
    private String patientName;

    private LocalDate prescriptionDate;

    private List<String> medicines = new ArrayList<>();

    private String diagnosis;

    private String instructions;

    @CreatedDate
    private LocalDateTime createdAt;

}
