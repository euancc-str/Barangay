package org.example.Users;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
public class BarangayAsset {
    private int assetId;
    private String itemName;      // e.g. "Lenovo Laptop"
    private String propertyNumber;// e.g. "PROP-2023-001"
    private Date dateAcquired;
    private String status;        // "Good", "Damaged", "Lost"
    private double value;
    private String propertyCode;
    private String serialNumber;
    private String brand;
    private String model;
    private Date purchaseDate;
    private String fundSource;
    private int usefulLifeYears;
    private String custodian;
    private String location;
    private String borrowerName;
    private Date dateBorrowed;
    private Date expectedReturnDate;

}