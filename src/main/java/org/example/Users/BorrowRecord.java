package org.example.Users;

import lombok.Data;
import java.sql.Date;

@Data
public class BorrowRecord {
    private int borrowId;
    private int assetId;
    private String assetName;   // Fetched via JOIN
    private int residentId;
    private String borrowerName;// Fetched via JOIN
    private Date dateBorrowed;
    private Date expectedReturnDate;
    private Date dateReturned;
    private String status;
    private String remarks;
}