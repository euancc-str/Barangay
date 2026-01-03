package org.example.Users;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Household {
    private int householdId;
    private String householdNo;
    private String purok;
    private String street;
    private String address;
    private int householdHeadId; // Links to Resident ID
    private int totalMembers;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String ownershipType;
    private String monthlyIncome;
    private boolean is4PsBeneficiary;
    private String familyType;
    private String fullName;
    private String electricitySource;
}