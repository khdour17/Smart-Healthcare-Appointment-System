package org.example.healthcare.repository.sql;

import org.example.healthcare.models.enums.TimeSlotStatus;
import org.example.healthcare.models.sql.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {


}