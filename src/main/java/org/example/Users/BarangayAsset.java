package org.example.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarangayAsset {
    private int assetId;
    private String itemName;      // e.g. "Lenovo Laptop"
    private String propertyNumber;// e.g. "PROP-2023-001"
    private Date dateAcquired;
    private String status;        // "Good", "Damaged", "Lost"
    private double value;         // Cost/Price
}