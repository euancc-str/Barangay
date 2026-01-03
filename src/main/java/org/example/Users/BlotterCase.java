package org.example.Users;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlotterCase {
    private int caseId;
    private String caseNumber;
    private LocalDate dateRecorded;
    private LocalTime timeRecorded;
    private String complainant;
    private String respondent;
    private String victim;
    private String incidentType;
    private String location;
    private String narrative;
    private String witnesses;
    private String status;
    private LocalDate hearingDate;
    private String officerInCharge;
    private String resolution;
}