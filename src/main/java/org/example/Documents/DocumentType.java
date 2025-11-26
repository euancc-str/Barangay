package org.example.Documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentType {
    private int docTypeId;         // PK
    private String name;           // Barangay Clearance, Indigency, etc.
    private double fee;
    private int processingTime;    // in days
    private int validityPeriod;    // in months
    private String category;
    private String description;
    private boolean requiresSignature;
    private String formatTemplate;
    private String issuingAuthority;
    private boolean renewable;
    private int copiesAllowed;
    private String documentCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastName;
    private String middleName;
    private String suffix;
    private int age;
    private LocalDateTime dob;
    private String civilStatus;
    private LocalDateTime dateNow;
    private String address;
    private String purpose;
}
