package org.example.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessEstablishment {
    private int businessId;
    private int ownerId;
    private String ownerName; // Helpful for display
    private String businessName;
    private String businessNature;
    private String ownershipType;
    private String purok;
    private String streetAddress;
    private LocalDate dateEstablished;
    private int employeeCount;
    private String buildingType;
    private String permitStatus;
    private String permitNumber;
    private LocalDate lastRenewalDate;
    private double capitalInvestment;
    private String electricitySource; // WattWise integration
}