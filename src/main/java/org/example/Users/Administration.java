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
public class Administration {
    private int adminId;
    private String termName;      // e.g. "2023-2026 Administration"
    private Date termStart;
    private Date termEnd;
    private String captainName;   // The leader of this term
    private String status;        // "Active" or "Ended"
    private String vision;        // Mission/Vision statement
}